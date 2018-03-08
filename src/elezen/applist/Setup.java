package elezen.applist;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.Collator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import elezen.applist.CheckBoxAdapter.ViewCache;
import elezen.standby_killer.R;
import elezen.standby_killer.StandbyKiller;

public class Setup extends Activity {
	final static public String TAG="StandbyKiller";
	final static public String FILENAME="killlist.txt";
	final static public String RETAINING="retainingTopApp=true";
	private Button mBtnOk;
	private Button mBtnCancle;
	private ListView mListView;
	private CheckBox mCkRetainingTopApp;
//	private List<HashMap<String,Object>> mList;  
	private CheckBoxAdapter mCheckBoxAdapter;  
//	private List<String> mListStr = new ArrayList<String>();  
	private AppInfo[] mApps = null; //用来存储获取的应用信息数据
	private Set<String> mSelectedPkgs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup);
		mSelectedPkgs=readList(FILENAME);
		getList(mSelectedPkgs);
		InitViews();
        Intent i= new Intent(Intent.ACTION_RUN);
        i.setClass(this, StandbyKiller.class);
        startService(i);
		
	}

	private void InitViews() {  

        mBtnOk = (Button) findViewById(R.id.bt_ok);  
        mBtnCancle = (Button) findViewById(R.id.bt_cancel);  
        mListView = (ListView) findViewById(R.id.listv);  
        mCheckBoxAdapter = new CheckBoxAdapter(this,mApps);  
        mListView.setAdapter(mCheckBoxAdapter);  
        mCkRetainingTopApp=(CheckBox)findViewById(R.id.cktop);
		mCkRetainingTopApp.setChecked(mSelectedPkgs.contains(RETAINING));        
        
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {  
  
            @Override  
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {

                ViewCache viewCache = (ViewCache) view.getTag();
                viewCache.cb.toggle();
                mApps[position].selected=viewCache.cb.isChecked();
                  
                mCheckBoxAdapter.notifyDataSetChanged();  
                  
            }


        });  
          
        mBtnOk.setOnClickListener(new android.view.View.OnClickListener(){

			@Override
			public void onClick(View v) {
				saveList(FILENAME);
				StandbyKiller.reloadList();
				Setup.this.finish();
			}
        	
        });  
        mBtnCancle.setOnClickListener(new android.view.View.OnClickListener(){

			@Override
			public void onClick(View v) {
				Setup.this.finish();
			}
        	
        });  
    }
	private void getList(Set<String> selectedPkgs){
		String s;
		PackageManager pkgMng=this.getPackageManager();
		List<PackageInfo> packages = pkgMng.getInstalledPackages(0);
		if(packages==null){
			mApps=new AppInfo[1];
			mApps[0]=new AppInfo();
			return;
		}
		int n=packages.size();
		mApps=new AppInfo[n];
		for(int i=0;i<n;i++) {
			PackageInfo packageInfo = packages.get(i);
			AppInfo tmpInfo =new AppInfo();
			tmpInfo.appName = packageInfo.applicationInfo.loadLabel(pkgMng).toString();
			s=packageInfo.packageName;
			tmpInfo.packageName =s;
			tmpInfo.selected=selectedPkgs.contains(s);
//			tmpInfo.versionName = packageInfo.versionName;
//			tmpInfo.versionCode = packageInfo.versionCode;
			tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(pkgMng);
			mApps[i]=tmpInfo;
		}
		sortList();
	}
	private void sortList(){
		int i,j,k,n;
		AppInfo t;
		n=mApps.length;
		Collator col=Collator.getInstance(java.util.Locale.CHINA); 
		col.setStrength(Collator.PRIMARY);
		for(i=0;i<n-1;i++){
			k=i;
			for(j=i+1;j<n;j++){
				boolean b1=mApps[k].selected;
				boolean b2=mApps[j].selected;
				if( !b1 && b2)k=j;
				else if(b1 == b2 && col.compare(mApps[k].appName, mApps[j].appName)>0)k=j;
			}
			if(k!=i){
				t=mApps[k];
				mApps[k]=mApps[i];
				mApps[i]=t;
			}
		}
	}
	private Set<String>  readList(String filename){
		return readList(this,filename);
	}
    public static Set<String>  readList(Context ct,String filename){
    	Set<String> set=new HashSet<String>();
        try {
        	FileInputStream fis=ct.openFileInput(filename);
            InputStreamReader inputreader = new InputStreamReader(fis);
            BufferedReader buffreader = new BufferedReader(inputreader);
            String line;
            while (( line = buffreader.readLine()) != null) {
            	line=line.trim();
              	if(line.length()>0)set.add(line);
//              	Log.e(TAG,line);
            }
            inputreader.close();
            fis.close();
        }catch (IOException e){
            Log.e(TAG, e.getMessage());
        }
        return set;
    }
    private void saveList(String filename){
	    try{
	    	FileOutputStream fout = openFileOutput(filename, Context.MODE_PRIVATE);
	    	OutputStreamWriter osw=new OutputStreamWriter(fout);
	    	for(AppInfo a:mApps){
	    		if(a.selected){
	    			osw.write(a.packageName);
	    			osw.write("\n");
	    		}
	    	}
//****save retaining top app : retaing=true
	    	if(mCkRetainingTopApp.isChecked()){
	    		osw.write(RETAINING);
	    		osw.write("\n");
//	    		Log.e(TAG,RETAINING);
	    	}
	    	
	        osw.close();
	        fout.close();
//	        Toast.makeText(Setup.this, "Saved", Toast.LENGTH_LONG).show();
	    }catch(IOException e){Log.e(TAG, e.getMessage());}
    }
}
