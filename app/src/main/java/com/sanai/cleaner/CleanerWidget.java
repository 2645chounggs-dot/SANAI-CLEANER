package com.sanai.cleaner;
import android.app.PendingIntent;import android.appwidget.*;import android.content.*;import android.os.*;import android.widget.RemoteViews;
public class CleanerWidget extends AppWidgetProvider {
 @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){render(c,m,ids,R.layout.widget);}
 static void render(Context c,AppWidgetManager m,int[] ids,int layout){
  StatFs s=new StatFs(Environment.getDataDirectory().getPath());
  String free=String.format(java.util.Locale.KOREA,"%.1f GB 여유",s.getAvailableBytes()/1e9);
  long n=c.getSharedPreferences("cleaner",0).getLong("candidate",-1);
  for(int id:ids){RemoteViews v=new RemoteViews(c.getPackageName(),layout);v.setTextViewText(R.id.widget_space,free);v.setTextViewText(R.id.widget_candidates,n<0?"스캔 필요":String.format(java.util.Locale.KOREA,"최근 후보 %.1f GB",n/1e9));Intent i=new Intent(c,MainActivity.class).setAction("com.sanai.cleaner.OPEN_SCAN");v.setOnClickPendingIntent(R.id.widget_open,PendingIntent.getActivity(c,id,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));m.updateAppWidget(id,v);}
 }
 static void refresh(Context c){AppWidgetManager m=AppWidgetManager.getInstance(c);render(c,m,m.getAppWidgetIds(new ComponentName(c,CleanerWidget.class)),R.layout.widget);render(c,m,m.getAppWidgetIds(new ComponentName(c,CompactWidget.class)),R.layout.widget_compact);}
}