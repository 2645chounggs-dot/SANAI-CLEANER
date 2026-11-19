package com.sanai.cleaner;
import android.appwidget.*;import android.content.*;
public class CompactWidget extends CleanerWidget { @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){render(c,m,ids,R.layout.widget_compact);} }