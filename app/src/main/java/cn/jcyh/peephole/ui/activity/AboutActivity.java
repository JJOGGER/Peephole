package cn.jcyh.peephole.ui.activity;

import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.List;
import java.util.Map;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;

public class AboutActivity extends BaseActivity {
    private ListView listview;
    private List<Map<String, Object>> dataList;
    private SimpleAdapter adapter;
    private String mAndroid;
    private String mBuildNumber;
    private String setnumber[];

    @Override
    public int getLayoutId() {
        return R.layout.activity_about;
    }

//    @Override
//    protected void init() {
//        //得到版本号|系统版本信息
//        getsystem();
//        // 初始化数据
//        initData();
//
//        String[] from = {"title", "get_data"};
//
//        int[] to = {R.id.about_title, R.id.about_get};
//
//        adapter = new SimpleAdapter(AboutActivity.this, dataList,
//                R.layout.aboutlist_item, from, to);
//        listview.setAdapter(adapter);
//        listview.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//                                    long arg3) {
//                if (arg2 == 2) {//升级
//                    Toast.makeText(AboutActivity.this, "升级", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }
//
//    private void getsystem() {
//        mAndroid = android.os.Build.VERSION.RELEASE;
//        mBuildNumber = android.os.Build.MODEL;
//    }
//
//    private void initData() {
//
//        String title[] = getResources().getStringArray(R.array.aboutlist_item);
//        String get_data[] = {mAndroid, mBuildNumber, ""};
//        dataList = new ArrayList<Map<String, Object>>();
//        for (int i = 0; i < title.length; i++) {
//            Map<String, Object> map = new HashMap<String, Object>();
//            map.put("title", title[i]);
//            map.put("get_data", get_data[i]);
//            dataList.add(map);
//        }
//    }
}
