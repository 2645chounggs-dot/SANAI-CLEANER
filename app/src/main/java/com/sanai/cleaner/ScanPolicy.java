package com.sanai.cleaner;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.security.cert.CertificateFactory;
import java.util.Base64;

/** Signed data-only updates. No remote executable code, APK installation or deletion commands. */
final class ScanPolicy {
 static final String CHANNEL="https://github.com/2645chounggs-dot/SANAI-CLEANER/releases/download/ota-channel/";
 static final int NATIVE=2;
 final int thumbnail,workers,prefixBytes,revision;
 ScanPolicy(int thumb,int workers,int prefix,int revision){this.thumbnail=thumb;this.workers=workers;this.prefixBytes=prefix;this.revision=revision;}
 static ScanPolicy defaults(){return new ScanPolicy(256,2,65536,0);}
 static ScanPolicy parse(String json)throws Exception{
  JSONObject j=new JSONObject(json);
  if(j.length()!=5||j.getInt("nativeVersion")!=NATIVE)throw new IOException("지원하지 않는 정책");
  int r=j.getInt("revision"),t=j.getInt("thumbnail"),w=j.getInt("workers"),p=j.getInt("prefixBytes");
  if(r<1||t<160||t>384||w<1||w>3||p<16384||p>262144)throw new IOException("안전 범위 초과");
  return new ScanPolicy(t,w,p,r);
 }
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("scan_policy",0);}
 static ScanPolicy load(Context c){try{String json=prefs(c).getString("json",""),sig=prefs(c).getString("sig","");if(json.isEmpty())return defaults();verify(c,json.getBytes(java.nio.charset.StandardCharsets.UTF_8),Base64.getDecoder().decode(sig));return parse(json);}catch(Exception e){return defaults();}}
 static final class Offer{final String json,sig;final ScanPolicy policy;Offer(String j,String s,ScanPolicy p){json=j;sig=s;policy=p;}}
 static byte[] get(String name,int max)throws Exception{
  URL url=new URL(CHANNEL+name);
  for(int redirect=0;redirect<5;redirect++){
   String host=url.getHost();if(!"https".equals(url.getProtocol())||!(host.equals("github.com")||host.equals("release-assets.githubusercontent.com")||host.equals("objects.githubusercontent.com")))throw new IOException("허용되지 않은 다운로드 주소");
   HttpURLConnection conn=(HttpURLConnection)url.openConnection();conn.setConnectTimeout(10000);conn.setReadTimeout(10000);conn.setInstanceFollowRedirects(false);
   try{
    int code=conn.getResponseCode();
    if(code>=300&&code<400){String loc=conn.getHeaderField("Location");if(loc==null)throw new IOException("리디렉션 오류");url=new URL(url,loc);continue;}
    if(code!=200)throw new IOException("채널 응답 "+code);
    try(InputStream in=conn.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1){if(out.size()+n>max)throw new IOException("파일 크기 초과");out.write(buf,0,n);}return out.toByteArray();}
   }finally{conn.disconnect();}
  }throw new IOException("리디렉션 한도 초과");
 }
 static void verify(Context c,byte[] data,byte[] sig)throws Exception{
  Signature[] signers=c.getPackageManager().getPackageInfo(c.getPackageName(),PackageManager.GET_SIGNING_CERTIFICATES).signingInfo.getApkContentsSigners();
  if(signers==null||signers.length!=1)throw new IOException("앱 서명 확인 실패");
  java.security.PublicKey key=CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(signers[0].toByteArray())).getPublicKey();
  String algorithm=key.getAlgorithm().equalsIgnoreCase("EC")?"SHA256withECDSA":"SHA256withRSA";
  java.security.Signature verifier=java.security.Signature.getInstance(algorithm);verifier.initVerify(key);verifier.update(data);if(!verifier.verify(sig))throw new IOException("정책 서명 불일치");
 }
 static Offer check(Context c)throws Exception{
  byte[] data=get("policy.json",4096),sig=get("policy.sig",4096);verify(c,data,sig);
  String json=new String(data,java.nio.charset.StandardCharsets.UTF_8);ScanPolicy policy=parse(json);
  if(policy.revision<=prefs(c).getInt("highest",0))return null;
  return new Offer(json,Base64.getEncoder().encodeToString(sig),policy);
 }
 static synchronized boolean apply(Context c,Offer offer)throws Exception{
  verify(c,offer.json.getBytes(java.nio.charset.StandardCharsets.UTF_8),Base64.getDecoder().decode(offer.sig));
  ScanPolicy p=parse(offer.json);if(p.revision<=prefs(c).getInt("highest",0))return false;
  return prefs(c).edit().putString("json",offer.json).putString("sig",offer.sig).putInt("highest",p.revision).commit();
 }
 static void reset(Context c){prefs(c).edit().remove("json").remove("sig").apply();}
}
