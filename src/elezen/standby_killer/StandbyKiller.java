package elezen.standby_killer;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import elezen.applist.Setup;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class StandbyKiller extends Service {
	private boolean reciverRegistered=false;
	private ActivityManager mAm = null;
	private Method mForceStopPackage=null;
	private Set<String> mListSet=null;
	private boolean mKeepTopApp=false;
	private static StandbyKiller mSelf=null;
    private final BroadcastReceiver receiver = new BroadcastReceiver(){  
        @Override  
        public void onReceive(final Context context, final Intent intent) {  
        	clearProcess();
        }
    };
    static public void reloadList(){
    	if(mSelf!=null)mSelf.initList();
    }
    private void registerScreenOffReceiver(){
    	if(reciverRegistered)return;
        final IntentFilter filter = new IntentFilter();  
        filter.addAction(Intent.ACTION_SCREEN_OFF);  
        registerReceiver(receiver, filter); 
        reciverRegistered=true;
    }  	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	private void initList(){
		mListSet=Setup.readList(this,Setup.FILENAME);
		if(mListSet==null || mListSet.isEmpty())return;
		mKeepTopApp=mListSet.contains(Setup.RETAINING);
		
	}
	@Override
	public void onCreate() {
		super.onCreate();
		mSelf=this;
    	registerScreenOffReceiver();

        try {
            Class<?> activityManagerClass = Class.forName("android.app.ActivityManager");
            mAm = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

            mForceStopPackage = activityManagerClass.getMethod("forceStopPackage", String.class);
            mForceStopPackage.setAccessible(true);
        }
        catch ( ClassNotFoundException e ) {
            Log.e("MyActivityManager", "No Such Class Found Exception", e);
        }
        catch ( Exception e ) {
            Log.e("MyActivityManager", "General Exception occurred", e);
        }
        initList();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
    	if(reciverRegistered){
    		unregisterReceiver(receiver);
        	reciverRegistered=false;
    	}
	}
	private String getTopApp(List<RunningAppProcessInfo> infoList){
		for (RunningAppProcessInfo apif:infoList) {
			if(apif.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
				apif.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE){
				return trimProcessName(apif.processName);
			}
		}
		return "";
	}
    private  void clearProcess(){

//        Log.e(Setup.TAG,am.getClass().getName());
        if(mAm==null || mForceStopPackage==null)return;

		List<RunningAppProcessInfo> infoList = mAm.getRunningAppProcesses();
		if(infoList==null)return;
		String topApp=getTopApp(infoList);
		String pn;

		for (RunningAppProcessInfo apif:infoList) {
			pn=trimProcessName(apif.processName);
			if(mListSet.contains(pn)){
				if(mKeepTopApp && pn.equals(topApp))continue;
                for (String pkg : apif.pkgList) { 
                    try {
                        mForceStopPackage.invoke(mAm, new Object[] {pkg} );
                    } catch (Exception e) {
                        Log.e(Setup.TAG, "ForceStopPackag failed...", e);
                    }
                } 
			}
		}

	}

	private static String trimProcessName(String pn){
		int i;
		i=pn.indexOf(':');
		if(i>0)return pn.substring(0,i);
		else return pn;
	}

}
