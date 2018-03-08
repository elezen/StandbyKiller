package elezen.standby_killer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartBroadcastReceiver extends BroadcastReceiver {
	private static final String ACTION = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)){

            Intent i= new Intent(Intent.ACTION_RUN);
              i.setClass(context, StandbyKiller.class);
              i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              context.startService(i);
      }
	}

}
