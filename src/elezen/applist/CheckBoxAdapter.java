package elezen.applist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import elezen.standby_killer.R;

public class CheckBoxAdapter extends BaseAdapter {

    private AppInfo[] mApps;  
    private LayoutInflater mLayoutInflater;  
 
    public CheckBoxAdapter(Context context,AppInfo[] apps) {  
        mApps = apps;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
    }  
	@Override
	public int getCount() {
		return mApps.length;  
	}

	@Override
	public Object getItem(int position) {
		return mApps[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tv,tv2;
		CheckBox cb;
		ImageView iv;
        if(convertView == null){  
            convertView = mLayoutInflater.inflate(R.layout.setup_item, parent,false);  
              
            ViewCache viewCache = new ViewCache();  
            tv = (TextView) convertView.findViewById(R.id.item_tv);
            tv2= (TextView) convertView.findViewById(R.id.item_tv_title);
            cb = (CheckBox) convertView.findViewById(R.id.item_cb);  
            iv = (ImageView)  convertView.findViewById(R.id.imgv);
            viewCache.tv = tv;
            viewCache.tv2=tv2;
            viewCache.cb = cb;
            viewCache.iv = iv;
            convertView.setTag(viewCache);  
        }else  
        {  
            ViewCache viewCache = (ViewCache) convertView.getTag();  
            tv = viewCache.tv; 
            tv2= viewCache.tv2;
            cb = viewCache.cb; 
            iv = viewCache.iv;
        }  
        AppInfo app=mApps[position];
        tv.setText(app.appName);
        tv2.setText(app.packageName);
        cb.setChecked(app.selected); 
        iv.setImageDrawable(app.appIcon);
        return convertView;  	
    }
    public class ViewCache{  
        TextView tv,tv2;  
        CheckBox cb;
        ImageView iv;
    }  

}
