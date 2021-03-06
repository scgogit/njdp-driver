package com.njdp.njdp_drivers.items;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.njdp.njdp_drivers.R;
import com.njdp.njdp_drivers.changeDefault.SpinnerAdapter;
import com.njdp.njdp_drivers.changeDefault.SpinnerAdapter_up_white_1;
import com.njdp.njdp_drivers.changeDefault.SpinnerAdapter_white;
import com.njdp.njdp_drivers.db.AppConfig;
import com.njdp.njdp_drivers.db.AppController;
import com.njdp.njdp_drivers.db.DriverDao;
import com.njdp.njdp_drivers.db.FieldInfoDao;
import com.njdp.njdp_drivers.db.SavedFieldInfoDao;
import com.njdp.njdp_drivers.db.SessionManager;
import com.njdp.njdp_drivers.login;
import com.njdp.njdp_drivers.mainpages;
import com.njdp.njdp_drivers.slidingMenu;
import com.njdp.njdp_drivers.util.CommonUtil;
import com.njdp.njdp_drivers.util.NetUtil;
import com.squareup.timessquare.CalendarPickerView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import bean.BasicDeployRes;
import bean.FieldInfo;
import bean.FieldInfoPost;
import okhttp3.Call;


public class item_intelligent_resolution extends Fragment implements View.OnClickListener {

    private static final String TAG = item_intelligent_resolution.class.getSimpleName();
    private slidingMenu mainMenu;
    private DrawerLayout menu;
    private com.beardedhen.androidbootstrap.BootstrapButton hintButton;

    ///////////////////////////////////日期选择器变量/////////////////////
    private TextView t_startDate;
    private TextView t_endDate;
    private CalendarPickerView calendarPickerView;
    private View dateView;//日期选择View
    private View parentView;//主View
    private PopupWindow datePickerPop;//日期选择器弹出
    private boolean popup_flag=false;//日期选择器是否弹出的标志
    private ArrayList<Date> dates = new ArrayList<Date>();//选择的起始日期
    ///////////////////////////////////日期选择器变量/////////////////////

    private Spinner sp_area;
    private Spinner sp_type;
    private ArrayAdapter areaAdapter;
    private ArrayAdapter typeAdapter;
    //范围下拉项选择是否改动的标志，默认都未改动
    private boolean sl_area_flag=false;
    private boolean sl_type_flag=false;
    private ProgressDialog pDialog;
    private String url;//服务器地址
    private String sl_area="50";
//    private String sl_type="小麦";
    private SessionManager sessionManager;
    private CommonUtil commonUtil;
    private NetUtil netUtil;
    private Gson gson;
    private FieldInfoDao fieldInfoDao;
    private String token;
    private String deploy_id;
    private String norm_id;
    private String startTime;
    private String endTime;
    private String machine_id;
    private List<FieldInfo> fieldInfos=new ArrayList<FieldInfo>();//返回的农田信息
    private String GPS_longitude="1.1";//GPS经度
    private String GPS_latitude="1.1";//GPS纬度
    private boolean text_gps_flag = false;//GPS定位是否成功
    private Date first_date;
    private SimpleDateFormat format;
    private SimpleDateFormat format2;
    ////////////////////////地图变量//////////////////////////
    private TextureMapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private boolean isFristLocation = true;
    /**
     * 当前定位的模式
     */
    private LocationService locationService;
    /**
     * 当前定位的模式
     */
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
    /**
     * 定位的客户端
     */
    private String permissionInfo;
    private final int SDK_PERMISSION_REQUEST = 127;
    private BDLocation curlocation ; //当前位置
    ////////////////////////地图变量//////////////////////////


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ////////////////////////地图代码////////////////////////////////////
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getActivity().getApplicationContext());
        ////////////////////////地图代码结束////////////////////////////////////

        View view = inflater.inflate(R.layout.activity_1_intelligent_resolution, container,
                false);
        mainMenu=(slidingMenu)getActivity();
        menu=mainMenu.drawer;

        fieldInfoDao=new FieldInfoDao(mainMenu);
        sessionManager=new SessionManager(getActivity());
        commonUtil=new CommonUtil(mainMenu);
        netUtil=new NetUtil(mainMenu);
        gson=new Gson();
        format = new SimpleDateFormat("yyyy年MM月dd日");
        format2= new SimpleDateFormat("yyyy-MM-dd");
        url=AppConfig.URL_GETFIELD;
        token=sessionManager.getToken();
        pDialog = new ProgressDialog(mainMenu);
        pDialog.setCancelable(false);

        view.findViewById(R.id.getback).setOnClickListener(this);
        view.findViewById(R.id.menu).setOnClickListener(this);
        view.findViewById(R.id.my_location).setOnClickListener(this);
        view.findViewById(R.id.arrange_button).setOnClickListener(this);
        view.findViewById(R.id.jobDate).setOnClickListener(this);
        t_startDate=(TextView)view.findViewById(R.id.startDate);
        t_endDate=(TextView)view.findViewById(R.id.endDate);
        sp_type=(Spinner)view.findViewById(R.id.type);
        sp_area=(Spinner)view.findViewById(R.id.area);

        parentView = LayoutInflater.from(mainMenu).inflate(R.layout.activity_1_intelligent_resolution, null);
        dateView = mainMenu.getLayoutInflater().inflate(R.layout.datepicker, null);
        dateInit();//日期选择初始化
        initDatePicker();//日历选择器监听
        dateView.findViewById(R.id.getBack).setOnClickListener(this);
        dateView.findViewById(R.id.getHelp).setOnClickListener(this);
        hintButton=(com.beardedhen.androidbootstrap.BootstrapButton)dateView.findViewById(R.id.hintButton);
        calendarPickerView.setOnDateSelectedListener(new clDateSelectedListener());//选一个范围的日期
        calendarPickerView.setCellClickInterceptor(new clCellClick());//选一天的日期

        //下拉菜单选择项初始化
        //范围
        areaAdapter= new SpinnerAdapter_up_white_1(mainMenu,getResources().getStringArray(R.array.area));
        sp_area.setAdapter(areaAdapter);
        sp_area.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sl_area_flag = true;
                sl_area = sp_area.getSelectedItem().toString();
                switch (sl_area)
                {
                    case "50公里":
                        sl_area="50";
                        break;
                    case "80公里":
                        sl_area="80";
                        break;
                    case "100公里":
                        sl_area="100";
                        break;
                    case "默认距离":
                        sl_area="50";
                        break;
                }
                gps_MachineLocation(machine_id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //作物类型
//        typeAdapter= new SpinnerAdapter(mainMenu, getResources().getStringArray(R.array.crop_type));
//        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        sp_type.setAdapter(typeAdapter);
//        sp_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                sl_type_flag = true;
////                sl_type = sp_type.getSelectedItem().toString();
//                initFieldInfo(sl_area);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });


        ///////////////////////////////////////json测试////////////////////////////////////////////
//        StringBuffer[] test=new StringBuffer[]{new StringBuffer("1"),new StringBuffer("5"),new StringBuffer("6"),new StringBuffer("4")};
//        Log.e(TAG,gson.toJson(test));
//        String test2="{\"status\":0,\"result\":[{\"plan_id\":52,\"income\":13177,\"cost\":1218.655,\"route\":[3,2,1,4,5]},{\"plan_id\":53,\"income\":13172,\"cost\":1218.655,\"route\":[3,2,1,5,4]},{\"plan_id\":54,\"income\":13171,\"cost\":1224.155,\"route\":[2,3,1,4,5]},{\"plan_id\":55,\"income\":13170,\"cost\":1225.155,\"route\":[1,3,2,4,5]},{\"plan_id\":56,\"income\":13167,\"cost\":1224.155,\"route\":[2,3,1,5,4]}]}";
//        mainMenu.basicDeployRess=gson.fromJson(test2,new TypeToken<List<BasicDeployRes>>(){}.getType());
//        Log.e(TAG,mainMenu.basicDeployRess.get(0).getRoute()[0].toString());

        ///////////////////////////////////////json测试////////////////////////////////////////////


        //////////////////////////地图代码////////////////////////////
        //获取地图控件引用

        mMapView = (TextureMapView) getActivity().findViewById(R.id.bmapView);
        mMapView = (TextureMapView) view.findViewById(R.id.bmapView);
        mMapView.showScaleControl(true);

        mBaiduMap = mMapView.getMap();

        // 改变地图状态
        //MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
        //mBaiduMap.setMapStatus(msu);

        //注册回到当前位置按钮监听事件
        ImageButton locationBtn = (ImageButton)view.findViewById(R.id.my_location);
        locationBtn.setOnClickListener(new goBackListener());

        //给服务器传递参数，启动该Activity获取50公里的维修站点经纬度
        Log.i(TAG, "启动activity获取50公里农田经纬度");


        // 开启图层定位
        // -----------location config ------------

        locationService = ((AppController) getActivity().getApplication()).locationService;
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的

        //注册监听
        locationService.registerListener(new mListener());
        locationService.setLocationOption(locationService.getOption());


        mBaiduMap.setMyLocationEnabled(true);
        locationService.start();// 定位SDK

        /////////////////地图代码结束////////////////////////


        //连接服务器自动定位
        try {
            machine_id = new DriverDao(mainMenu).getDriver(1).getMachine_id();
            Log.e(TAG, machine_id);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        gps_MachineLocation(machine_id);//获取GPS位置,经纬度信息



        //////////////////////////////地图代码///////////////
        //定义Maker坐标点
        //西廉良村，河北大学，东站,保定站,植物园
        //Double[][] numthree = new Double[][]{{38.885335516312644, 115.44805233879083}, {38.86858730724386, 115.51474000000007}, {38.86430366154974, 115.60169999999994},
        //{38.86317366367406, 115.47990000000006}, {38.914613417728475, 115.4850954388619}};
        Log.i("nnnnnnnnnnnnnnnnnn", String.valueOf(mainMenu.selectedFieldInfo.size()));
        //this.markRepairStation(mainMenu.selectedFieldInfo);

        //////////////////地图代码结束/////////////////////////

        return view;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getback:
                mainMenu.finish();
                break;
            case R.id.menu:
                menu.openDrawer(Gravity.LEFT);
                break;
            case R.id.my_location:
                gps_MachineLocation(machine_id);//定位我的位置
                break;
            case R.id.arrange_button:
//                //////////////////测试,需要替换//////////////////////////////////////////////////
//                Search();
//                //////////////////测试,需要替换/////////////////////////////////////////////////
                if((!GPS_latitude.equals("1.1"))&&(!GPS_longitude.equals("1.1"))) {
                    Search();
                }else {
                    commonUtil.error_hint("定位失败,请点击点位按钮！");
                }
                break;
            case R.id.jobDate:
                popup_flag=true;
                datePickerPop.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.getBack:
                popup_flag=false;
                datePickerPop.dismiss();
                break;
            case R.id.getHelp:
                commonUtil.error_hint("请选择作业起止日期！");
                break;
            default:
                break;
        }
    }

    //监听返回按键
    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    if(popup_flag)
                    {
                        popup_flag=false;
                        datePickerPop.dismiss();
                        return true;
                    }else
                    {
                        mainMenu.finish();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    //跳转到选择界面
    private void Search()
    {

    ////////////////////////////////////////////测试数据test////////////////////////////////////////
//        try {
//            String response = "{\"status\":0,\"deploy_id\":85,\"norm_id\":10," +
//                    "\"machine_farm_d\":[{\"farm_id\":4,\"distance\":\"301.95\"}," +
//                    "{\"farm_id\":5,\"distance\":\"301.95\"}," +
//                    "{\"farm_id\":6,\"distance\":\"301.95\"}," +
//                    "{\"farm_id\":2,\"distance\":\"309.805\"}," +
//                    "{\"farm_id\":3,\"distance\":\"309.805\"}," +
//                    "{\"farm_id\":1,\"distance\":\"309.805\"}]," +
//                    "\"farms\":[" +
//                    "{\"farm_id\":1,\"user_name\":\"12345678901\",\"crops_kind\":\"小麦\",\"area_num\":\"28.0\",\"unit_price\":\"68.0\",\"block_type\":\"规则\",\"province\":\"河北\",\"city\":\"邯郸\",\"county\":\"肥乡县\",\"town\":\"测试乡\",\"village\":\"成功村\",\"longitude\":\"115.45580333333334\",\"latitude\":\"38.920224999999995\",\"start_time\":\"2016-04-28\",\"end_time\":\"2016-04-29\"}," +
//                    "{\"farm_id\":2,\"user_name\":\"12345678901\",\"crops_kind\":\"小麦\",\"area_num\":\"28.0\",\"unit_price\":\"68.0\",\"block_type\":\"规则\",\"province\":\"河北\",\"city\":\"邯郸\",\"county\":\"肥乡县\",\"town\":\"测试乡\",\"village\":\"成功村\",\"longitude\":\"115.45580333333334\",\"latitude\":\"38.920224999999995\",\"start_time\":\"2016-04-28\",\"end_time\":\"2016-04-29\"}," +
//                    "{\"farm_id\":3,\"user_name\":\"12345678901\",\"crops_kind\":\"小麦\",\"area_num\":\"58.0\",\"unit_price\":\"68.0\",\"block_type\":\"规则\",\"province\":\"河北\",\"city\":\"邯郸\",\"county\":\"大名县\",\"town\":\"测试乡\",\"village\":\"成功村\",\"longitude\":\"115.45580333333334\",\"latitude\":\"38.920224999999995\",\"start_time\":\"2016-04-28\",\"end_time\":\"2016-04-29\"}," +
//                    "{\"farm_id\":4,\"user_name\":\"12345678901\",\"crops_kind\":\"小麦\",\"area_num\":\"50.0\",\"unit_price\":\"68.0\",\"block_type\":\"规则\",\"province\":\"河北\",\"city\":\"邯郸\",\"county\":\"峰峰矿区\",\"town\":\"测试乡\",\"village\":\"成功村\",\"longitude\":\"115.47697500000001\",\"latitude\":\"38.85114166666666\",\"start_time\":\"2016-04-29\",\"end_time\":\"2016-04-30\"}," +
//                    "{\"farm_id\":5,\"user_name\":\"12345678901\",\"crops_kind\":\"小麦\",\"area_num\":\"50.0\",\"unit_price\":\"68.0\",\"block_type\":\"规则\",\"province\":\"河北\",\"city\":\"邯郸\",\"county\":\"峰峰矿区\",\"town\":\"测试乡\",\"village\":\"成功村\",\"longitude\":\"115.47697500000001\",\"latitude\":\"38.85114166666666\",\"start_time\":\"2016-04-29\",\"end_time\":\"2016-04-30\"}," +
//                    "{\"farm_id\":6,\"user_name\":\"12345678901\",\"crops_kind\":\"小麦\",\"area_num\":\"58.0\",\"unit_price\":\"68.0\",\"block_type\":\"规则\",\"province\":\"河北\",\"city\":\"邯郸\",\"county\":\"成安县\",\"town\":\"测试乡\",\"village\":\"成功村\",\"longitude\":\"115.47697500000001\",\"latitude\":\"38.85114166666666\",\"start_time\":\"2016-04-29\",\"end_time\":\"2016-04-29\"}]}";
//
//            JSONObject jObj = new JSONObject(response);
//            deploy_id=jObj.getString("deploy_id");
//            norm_id=jObj.getString("norm_id");
//            JSONArray s_post=jObj.getJSONArray("machine_farm_d");
//            JSONArray s_info=jObj.getJSONArray("farms");
//            String s_t=jObj.getString("farms");
//            String s_p=jObj.getString("machine_farm_d");
//            sessionManager.setUserTag(deploy_id, norm_id);
//            Log.e(TAG, String.valueOf(s_post.length()));
//            Log.e(TAG, String.valueOf(s_info.length()));
//            fieldInfos=gson.fromJson(s_t,new TypeToken<List<FieldInfo>>() {}.getType());
//            mainMenu.fieldInfoPosts=gson.fromJson(s_p,new TypeToken<List<FieldInfoPost>>() {}.getType());
//            for(int i=0;i<mainMenu.fieldInfoPosts.size();i++)
//            {
//                FieldInfoPost ft=mainMenu.fieldInfoPosts.get(i);
//                Log.e("farm_id:",ft.getFarm_id());
//                Log.e("distance:",ft.getDistance());
//            }
//            Log.e(TAG, mainMenu.fieldInfoPosts.get(1).getDistance());
//            Log.e(TAG,fieldInfos.get(1).getCropLand_site());
//
//        }catch (Exception e){
//            e.printStackTrace();
//            Log.e(TAG,e.toString());
//        }
//        if( (fieldInfos.size()<1)||( mainMenu.fieldInfoPosts.size()<1)){
//            commonUtil.error_hint("未搜索到符合要求的农田信息，请重新设置搜索条件！");
//        }else
//        {
//            //存储到本地库
//            saveFieldInfo(fieldInfoDao,fieldInfos);
//            //按距离的排序好的农田
//            arg_FieldInfos=arrangeField();
//            pDialog.setMessage("正在准备数据，请等待 ...");
//            showDialog();
//
//            Fragment fragment = new item_intelligent_resolution_1();
//            mainMenu.addBackFragment(fragment);
//
//            hideDialog();
//        }
        ////////////////////////////////////////////测试数据test////////////////////////////////////

        if( mainMenu.selectedFieldInfo.size()<1){
            commonUtil.error_hint("未搜索到符合要求的农田信息，请重新设置搜索条件！");
        }else {
            pDialog.setMessage("正在准备数据，请等待 ...");
            showDialog();

            Fragment fragment = new item_intelligent_resolution_1();
            mainMenu.addBackFragment(fragment);
           hideDialog();
        }
    }

    //获取农田数据
//    private void initFieldInfo(final String area) {
//
//       pDialog.setMessage("正在加载 ...");
//       showDialog();
//
//        if (!netUtil.checkNet(mainMenu)) {
//            commonUtil.error_hint("网络连接错误");
//            hideDialog();
//        } else {
//            startTime = "2016-04-01";
//            endTime = "2016-05-01";
//            //服务器请求
//            Log.e(TAG, machine_id);
//            Log.e(TAG, token);
//            Log.e(TAG, area);
//            Log.e(TAG, startTime);
//            Log.e(TAG, endTime);
//            Log.e(TAG, GPS_longitude);
//            Log.e(TAG, GPS_latitude);
//
//            Map<String, String> params = new HashMap<String, String>();
//            Map<String, String> params2 = new HashMap<String, String>();
//            params.put("machine_id", machine_id);
//            params.put("token",token);
//            params.put("deploy_range", area);
//            params.put("deploy_startdate", startTime);
//            params.put("deploy_finishdate", endTime);
//            params.put("Machine_longitude", GPS_longitude);
//            params.put("Machine_Latitude", GPS_latitude);
//            params2=netUtil.checkParams(params);
//
//            OkHttpUtils.post()
//                    .url("http://172.28.145.4:8888/db_xskq/login.php")
//                    .params(params)
//                    .build()
//                    .writeTimeOut(20000)
//                    .readTimeOut(20000)
//                    .connTimeOut(20000)
//                    .execute(new Callback() {
//
//                        //xml格式数据解析
//                        @Override
//                        public Object parseNetworkResponse(okhttp3.Response response) throws Exception {
//                            return null;
//                        }
//
//                        //连接服务器错误
//                        @Override
//                        public void onError(Call call, Exception e) {
//
//                            Log.e(TAG, "GetFieldInfo Connect Error: " + e.getMessage());
//                            commonUtil.error_hint2_short(R.string.connect_error);
//                            hideDialog();
//                        }
//
//                        //Json数据解析
//                        @Override
//                        public void onResponse(Object response) {
//
//                            hideDialog();
//                            try {
////                                String json = gson.toJson(response);
//                                String json = response.toString();
//                                Log.d(TAG, "AreaInit Response: " + json);
//                                JSONObject jObj = new JSONObject(json);
//                                int status = jObj.getInt("status");
//                                if (status == 1) {
//                                    String errorMsg = jObj.getString("result");
//                                    Log.e(TAG, "Token 错误:" + errorMsg);
//                                    commonUtil.error_hint("密钥失效，请重新登录");
//                                    //清空数据，重新登录
//                                    netUtil.clearSession(mainMenu);
//                                    Intent intent = new Intent(mainMenu, login.class);
//                                    startActivity(intent);
//                                    mainMenu.finish();
//                                } else if (status == 0) {
//
//                                    deploy_id = jObj.getString("deploy_id");
//                                    norm_id = jObj.getString("norm_id");
//                                    Log.e(TAG, "deploy_id：" + deploy_id);
//                                    Log.e(TAG, "deploy_id：" + norm_id);
//                                    sessionManager.setUserTag(deploy_id, norm_id);
//                                    ///////////////////////////农田信息，包括经纬度/////////////////////////////////
//                                    JSONArray s_post = jObj.getJSONArray("machine_farm_d");
//                                    JSONArray s_info = jObj.getJSONArray("farms");
//                                    Log.e(TAG, String.valueOf(s_post.length()));
//                                    Log.e(TAG, String.valueOf(s_info.length()));
//
//                                    String s_t = jObj.getString("farms");
//                                    String s_p = jObj.getString("machine_farm_d");
//                                    List<FieldInfo> fieldInfos = gson.fromJson(s_t, new TypeToken<List<FieldInfo>>() {
//                                    }.getType());//存储农田信息
//                                    mainMenu.fieldInfoPosts = gson.fromJson(s_p, new TypeToken<List<FieldInfoPost>>() {
//                                    }.getType());//存储距离信息
//                                    Log.e(TAG, mainMenu.fieldInfoPosts.get(1).getDistance());
//                                    Log.e(TAG, fieldInfos.get(1).getCropLand_site());
//
//
//                                    ///////////////////////////农田信息，包括经纬度/////////////////////////////////
//
//                                    if ((fieldInfos.size() < 1) || (mainMenu.fieldInfoPosts.size() < 1)) {
//                                        commonUtil.error_hint("未搜索到符合要求的农田信息，请重新设置搜索条件！");
//                                    } else {
//                                        //存储到本地库
//                                        saveFieldInfo(fieldInfoDao, fieldInfos);
//                                        //按距离的排序好的农田
//                                        arg_FieldInfos = arrangeField();
//                                    }
//                                } else {
//
//                                    String errorMsg = jObj.getString("result");
//                                    Log.e(TAG, "1 Json error：response错误:" + errorMsg);
//                                    commonUtil.error_hint("服务器数据错误1：response错误:" + errorMsg);
//                                }
//                            } catch (JSONException e) {
//                                // JSON error
//                                Log.e(TAG, "2 服务器数据错误：response错误:" + e.getMessage());
//                                commonUtil.error_hint("服务器数据错误2：response错误:" + e.getMessage());
//                            }catch (Exception e)
//                            {
//                                // JSON error
//                                Log.e(TAG, "3 服务器数据错误：response错误:" + e.getMessage());
//                                commonUtil.error_hint("服务器数据错误3：response错误:" + e.getMessage());
//                            }
//                        }
//                    });
//        }
//
//    }

    ////////////////////////////////////获取农田信息////////////////////////////////////////////////
    private void initFieldInfo(final String area)
    {
            String tag_string_req = "req_init";
            mainMenu.pDialog.setMessage("正在载入 ...");
            mainMenu.showDialog();

            if (!netUtil.checkNet(mainMenu)) {
                commonUtil.error_hint("网络连接错误");
                mainMenu.hideDialog();
            } else {
                //////////////////////////////////////////////////////////////////////////////////
                startTime="2016-04-01";
                endTime="2016-06-01";
                //////////////////////////////////////////////////////////////////////////////////
                //服务器请求
                Log.e(TAG, machine_id);
                Log.e(TAG, token);
                Log.e(TAG, area);
                Log.e(TAG, startTime);
                Log.e(TAG, endTime);
                Log.e(TAG, GPS_longitude);
                Log.e(TAG, GPS_latitude);

                StringRequest strReq = new StringRequest(Request.Method.POST,
                        url, new initSuccessListener(), mainMenu.mErrorListener) {

                    @Override
                    protected Map<String, String> getParams() {

                        Map<String, String> params = new HashMap<String, String>();
                        params.put("machine_id", machine_id);
                        params.put("token", token);
                        params.put("deploy_range", area);//需要按照实际范围变动
                        params.put("deploy_startdate", startTime);
                        params.put("deploy_finishdate", endTime);
                        params.put("Machine_longitude", GPS_longitude);
                        params.put("Machine_Latitude", GPS_latitude);

                        return netUtil.checkParams(params);
                    }
                };
                strReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
                // Adding request to request queue
                AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
            }
        }

    private class initSuccessListener implements Response.Listener<String>//获取农田信息数据响应服务器成功
    {
        @Override
        public void onResponse(String response) {
            Log.e(TAG, "AreaInit Response: " + response);
            mainMenu.hideDialog();
            try {
                JSONObject jObj = new JSONObject(response);
                int status = jObj.getInt("status");
                if (status==1)
                {
                    String errorMsg = jObj.getString("result");
                    Log.e(TAG, "Json error：response错误:" + errorMsg);
                    commonUtil.error_hint("密钥失效，请重新登录");
                    //清空数据，重新登录
                    netUtil.clearSession(mainMenu);
                    Intent intent = new Intent(mainMenu, login.class);
                    startActivity(intent);
                    mainMenu.finish();
                }else if(status==0){

                    deploy_id=jObj.getString("deploy_id");
                    norm_id=jObj.getString("norm_id");
                    sessionManager.setUserTag(deploy_id, norm_id);
                    ///////////////////////////农田信息，包括经纬度/////////////////////////////////



                    /////////////////////////////////测试有多少个农田信息///////////////////////////
                    JSONArray s_post=jObj.getJSONArray("machine_farm_d");
                    JSONArray s_info=jObj.getJSONArray("farms");
                    Log.e(TAG, String.valueOf(s_post.length()));
                    Log.e(TAG, String.valueOf(s_info.length()));
                    /////////////////////////////////测试有多少个农田信息///////////////////////////


                    try {
                        fieldInfos.clear();
                    }catch (Exception e){}
                    try {
                        mainMenu.fieldInfoPosts.clear();
                    }catch (Exception e){}
                    String s_t=jObj.getString("farms");
                    String s_p=jObj.getString("machine_farm_d");
                    List<FieldInfo> fieldInfos=gson.fromJson(s_t,new TypeToken<List<FieldInfo>>() {}.getType());//存储农田信息
                    mainMenu.fieldInfoPosts=gson.fromJson(s_p,new TypeToken<List<FieldInfoPost>>() {}.getType());//存储距离信息
                    try {
                        Log.e(TAG, mainMenu.fieldInfoPosts.get(1).getDistance());
                        Log.e(TAG, fieldInfos.get(0).getCropLand_site());
                    }catch (Exception e)
                    {
                        Log.e(TAG,"没有获取到数据"+e.getMessage());
                    }

                    ///////////////////////////农田信息，包括经纬度/////////////////////////////////

                    if( (fieldInfos.size()<1)||( mainMenu.fieldInfoPosts.size()<1)){
                        commonUtil.error_hint("未搜索到符合要求的农田信息，请重新设置搜索条件！");
                    }else
                    {
                        //存储到本地库
                        saveFieldInfo(fieldInfoDao,fieldInfos);
                        //按距离的排序好的农田
                        arrangeField();
                        commonUtil.error_hint("数据加载完成");
                    }
                } else {

                    String errorMsg = jObj.getString("result");
                    Log.e(TAG, "1 Json error：response错误:" + errorMsg);
                    commonUtil.error_hint("服务器数据错误1：response错误:" + errorMsg);
                }
            } catch (JSONException e) {
                // JSON error
                Log.e(TAG, "2 服务器数据错误：response错误:" + e.getMessage());
                commonUtil.error_hint("服务器数据错误2：response错误:" + e.getMessage());
            }
        }
    }

    ////////////////////////////////////获取农田信息////////////////////////////////////////////////



    ////////////////////////////////////从服务器获取农机经纬度///////////////////////////////////////
    public void gps_MachineLocation(final String machine_id) {
        String tag_string_req = "req_GPS";
        //服务器请求
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_MACHINELOCATION, new locationSuccessListener(), new locationErrorListener()) {

            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();
                params.put("machine_id", machine_id);
                params.put("token", token);

                return netUtil.checkParams(params);
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    private class locationSuccessListener implements Response.Listener<String>//获取农机位置响应服务器成功
    {
        @Override
        public void onResponse(String response) {
            Log.d(TAG, "AreaInit Response: " + response);
            try {
                JSONObject jObj = new JSONObject(response);
                int status = jObj.getInt("status");

                if (status == 1) {
                    String errorMsg = jObj.getString("result");
                    Log.e(TAG, "Json error：response错误:" + errorMsg);
                    commonUtil.error_hint("密钥失效，请重新登录");
                    //清空数据，重新登录
                    netUtil.clearSession(mainMenu);
                    Intent intent = new Intent(mainMenu, login.class);
                    startActivity(intent);
                    mainMenu.finish();
                } else if (status == 0) {

                    ///////////////////////////获取服务器农机，经纬度/////////////////////
                    JSONObject location = jObj.getJSONObject("result");
                    GPS_longitude = location.getString("x");
                    GPS_latitude = location.getString("y");
                    ///////////////////////////获取服务器农机，经纬度/////////////////////

                    text_gps_flag = false;
                    isGetLocation();
                } else {
                    String errorMsg = jObj.getString("result");
                    Log.e(TAG, "GPSLocation Error:" + errorMsg);
                    text_gps_flag = true;
                    isGetLocation();
                }
            } catch (JSONException e) {
                // JSON error
                Log.e(TAG, "GPSLocation Error,Json error：response错误:" + e.getMessage());
                text_gps_flag = true;
                isGetLocation();
            }
        }
    }

    private class locationErrorListener implements  Response.ErrorListener//定位服务器响应失败
    {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "ConnectService Error: " + error.getMessage());
            netUtil.testVolley(error);
            text_gps_flag = true;
            isGetLocation();
        }
    }

    //定位成功或者失败后的响应
    private void isGetLocation() {
        if (!text_gps_flag) {
            Log.e(TAG, "GPS自动定位成功");
            LatLng ll = new LatLng(Double.valueOf(GPS_latitude),
                    Double.valueOf(GPS_longitude));
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(u);
            commonUtil.error_hint("GPS定位成功");
            initFieldInfo(sl_area);
        } else {
            Log.e(TAG, "GPS自动定位失败,开启百度定位！");
            try {
                GPS_latitude = String.valueOf(curlocation.getLatitude());
                GPS_longitude = String.valueOf(curlocation.getLongitude());
                initFieldInfo(sl_area);
                LatLng ll = new LatLng(curlocation.getLatitude(),
                        curlocation.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
            }catch (Exception e)
            {
                commonUtil.error_hint("自动定位失败，请重试！");
                hideDialog();
                Log.e(TAG, "Location Error:" + "自动定位失败" + e.getMessage());
            }
        }
    }
    ////////////////////////////////////从服务器获取农机经纬度///////////////////////////////////////


    //农田信息存入本地数据库
    private void saveFieldInfo(FieldInfoDao fieldInfoDao,List<FieldInfo> fieldInfos)
    {
        if(fieldInfoDao.countOfField()>0)
        {
            //本地库里是否存在农田信息，存在则删除
            List<FieldInfo> testFieldInfos=fieldInfoDao.allFieldInfo();
            for (int i = 0; i < testFieldInfos.size(); i++) {
                FieldInfo fieldInfo = testFieldInfos.get(i);
                fieldInfoDao.delete(fieldInfo);
                Log.e(TAG, "删除第" + String.valueOf(fieldInfo.getId()) + "条");
            }
        }
        for (int i = 0; i < fieldInfos.size(); i++) {
            FieldInfo fieldInfo = fieldInfos.get(i);
            fieldInfo.setId(i + 1);
            fieldInfoDao.add(fieldInfo);
            Log.e(TAG, "存储第" + String.valueOf(fieldInfo.getId()) + "条");
            Log.e(TAG, fieldInfoDao.getFieldInfo(i + 1).getFarm_id());
        }
        fieldInfos.clear();
    }

    //从本地读取，按mainMenu.fieldInfoPosts顺序排序存储到mainMenu.selectedFieldInfo
    private void arrangeField()
    {
        for(int i=0;i<mainMenu.fieldInfoPosts.size();i++) {
            Log.e(TAG, mainMenu.fieldInfoPosts.get(i).getFarm_id());
            try {
                FieldInfo fieldInfo = fieldInfoDao.getFieldInfoByFieldId(mainMenu.fieldInfoPosts.get(i).getFarm_id());
                Log.e(TAG, "找到一条：" + fieldInfo.getFarm_id());
                mainMenu.selectedFieldInfo.add(fieldInfo);
            }catch (Exception e)
            {
                Log.e(TAG, e.toString());
            }
        }
        this.markRepairStation(mainMenu.selectedFieldInfo);
    }

    //起始日期，终止日期初始化
    private void dateInit()
    {
        SimpleDateFormat format=new SimpleDateFormat("yyyy年MM月dd日");
        Calendar calendar = Calendar.getInstance();
        Date defaultStartDate=calendar.getTime();
        calendar.add(calendar.DATE, 1);
        Date defaultEndDate=calendar.getTime();
        t_startDate.setText("开始日期：" + format.format(defaultStartDate));
        t_endDate.setText("结束日期：" + format.format(defaultEndDate));//显示默认的起止年月日
        startTime=format2.format(defaultStartDate);
        endTime=format2.format(defaultEndDate);
    }

    //初始化日期选择器popupWindow
    private void initDatePicker() {
        Calendar maxDate=Calendar.getInstance();
        maxDate.add(Calendar.MONTH, 2);
        calendarPickerView=(CalendarPickerView)dateView.findViewById(R.id.calendarDatePicker);
        calendarPickerView.init(new Date(), maxDate.getTime())
                .inMode(CalendarPickerView.SelectionMode.RANGE);
        datePickerPop = new PopupWindow(dateView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        datePickerPop.setAnimationStyle(R.style.slideAnimation_bottom);
        datePickerPop.setOutsideTouchable(true);
    }

    ////////////////////////////地图代码开始//////////////////////////////////
    class mListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // 构造定位数据
            MyLocationData locData;
            if(!text_gps_flag){//成功获取农机经纬度
                locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                                // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100).latitude(Double.parseDouble(GPS_latitude))
                        .longitude(Double.parseDouble(GPS_longitude))
                        .build();
            }else{
                locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                                // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100).latitude(location.getLatitude())
                        .longitude(location.getLongitude())
                        .build();
            }

            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);
            // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
            BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_geo);
            MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, false, mCurrentMarker);
            mBaiduMap.setMyLocationConfigeration(config);
            Log.i("wwwwwwwwwwwwwwww", location.getLatitude() + "---" + location.getLongitude());


            //保存百度地图定位的当前位置
            curlocation = location;

            // 第一次定位时，将地图位置移动到当前位置，这里有问题，先定位到河北农业大学
            /*if (isFristLocation) {
                isFristLocation = false;

                //保存当前location
                curlocation = location;

                if(!text_gps_flag) {//成功获取农机位置
                    LatLng ll = new LatLng(Double.parseDouble(GPS_latitude),
                            Double.parseDouble(GPS_longitude));
                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                    mBaiduMap.animateMapStatus(u);
                }else{
                    LatLng ll = new LatLng(location.getLatitude(),
                            location.getLongitude());
                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                    mBaiduMap.animateMapStatus(u);
                }
            }*/
        }
    }

    //标记农田,参数经纬度
    private void markRepairStation(List<FieldInfo> fieldInfos) {
        //清楚覆盖物Marker,重新加载
        mBaiduMap.clear();

        Integer[] marks = new Integer[]{R.drawable.s1, R.drawable.s2, R.drawable.s3, R.drawable.s4, R.drawable.s5,
                R.drawable.s6, R.drawable.s7, R.drawable.s8, R.drawable.s9, R.drawable.s10,R.drawable.s11, R.drawable.s12,
                R.drawable.s13, R.drawable.s14, R.drawable.s15, R.drawable.s16, R.drawable.s17, R.drawable.s18, R.drawable.s19,
                R.drawable.s20, R.drawable.s21, R.drawable.s22, R.drawable.s23, R.drawable.s24, R.drawable.s25, R.drawable.s26,
                R.drawable.s27, R.drawable.s28, R.drawable.s29, R.drawable.s30};
        for (int i = 0; i < fieldInfos.size(); i++) {
            FieldInfo f = fieldInfos.get(i);
            LatLng point = new LatLng(f.getLatitude(), f.getLongitude());

            int icon ;
            if(i<30){
                icon=marks[i];
            }else{
                icon=R.drawable.icon_gcoding;
            }

            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromResource(icon);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions()
                    .position(point)
                    .icon(bitmap);
            //在地图上添加Marker，并显示
            Marker marker = (Marker) mBaiduMap.addOverlay(option);

            //FieldInfo fieldInfo = new FieldInfo();
            //fieldInfo.setLatitude(numthree[i][0]);
            //fieldInfo.setLongitude(numthree[i][1]);
            //fieldInfo.setTelephone("13483208987");

            Bundle bundle = new Bundle();
            bundle.putSerializable("fieldInfo", f);
            marker.setExtraInfo(bundle);

            //添加覆盖物鼠标点击事件
            mBaiduMap.setOnMarkerClickListener(new markerClicklistener());
        }

        mMapView.refreshDrawableState();
    }

    //地图图标点击事件监听类
    class markerClicklistener implements BaiduMap.OnMarkerClickListener {

        /**
         * 地图 Marker 覆盖物点击事件监听函数
         *
         * @param marker 被点击的 marker
         */
        @Override
        public boolean onMarkerClick(Marker marker) {
            final FieldInfo fieldInfo = (FieldInfo) marker.getExtraInfo().get("fieldInfo");
            InfoWindow infoWindow;

            //构造弹出layout
            LayoutInflater inflater = LayoutInflater.from(getActivity().getApplicationContext());
            View markerpopwindow = inflater.inflate(R.layout.markerpopwindow, null);

            TextView tv = (TextView) markerpopwindow.findViewById(R.id.markinfo);
            String markinfo = "位置:"+fieldInfo.getVillage()+"\n"+
                    "电话:" + fieldInfo.getUser_name() + "\n" +
                    "面积:" + fieldInfo.getArea_num() + "\n" +
                    "单价:"+fieldInfo.getUnit_price()+"\n"+
                    "开始时间:" + fieldInfo.getStart_time()+"\n"+
                    "结束时间:"+fieldInfo.getEnd_time();
            Log.i("markinfo", markinfo);
            tv.setText(markinfo);


            ImageButton tellBtn = (ImageButton) markerpopwindow.findViewById(R.id.markerphone);
            tellBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + fieldInfo.getUser_name()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });

            LatLng ll = marker.getPosition();
            //将marker所在的经纬度的信息转化成屏幕上的坐标
            Point p = mBaiduMap.getProjection().toScreenLocation(ll);
            p.y -= 90;
            LatLng llInfo = mBaiduMap.getProjection().fromScreenLocation(p);
            //初始化infoWindow，最后那个参数表示显示的位置相对于覆盖物的竖直偏移量，这里也可以传入一个监听器
            infoWindow = new InfoWindow(markerpopwindow, llInfo, 0);
            mBaiduMap.showInfoWindow(infoWindow);//显示此infoWindow
            //让地图以备点击的覆盖物为中心
            MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.setMapStatus(status);
            return true;
        }
    }

    //回到当前位置按钮点击事件,将当前位置定位到屏幕中心
    class goBackListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            GPS_latitude=String.valueOf(curlocation.getLatitude());
            GPS_longitude=String.valueOf( curlocation.getLongitude());
            LatLng ll = new LatLng(curlocation.getLatitude(),
                    curlocation.getLongitude());
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(u);

        }
    }

    ////////////////////////////地图代码结束/////////////////////////////////

    //日期选择单天监听
    private class clCellClick implements CalendarPickerView.CellClickInterceptor
    {
        @Override
        public boolean onCellClicked(Date date) {
            int size = (calendarPickerView.getSelectedDates()).size();
            if (size == 1) {
                hintButton.setText("请选择作业结束日期");
            }
            if (first_date == null) {
                hintButton.setText("请选择作业结束日期");
            }
            if(date==first_date)
            {
                String first_dateTime = format.format(date);
                t_startDate.setText(getResources().getString(R.string.jobSart) + first_dateTime);
                t_endDate.setText(getResources().getString(R.string.jobEnd) + first_dateTime);
                startTime = format2.format(date);
                endTime = format2.format(date);
                dates.clear();
                popup_flag=false;
                first_date= null;
                hintButton.setText("请选择作业开始日期");
                gps_MachineLocation(machine_id);
                datePickerPop.dismiss();
            }
            return false;
        }
    }
    //日期选择器范围时间监听
    private class clDateSelectedListener implements CalendarPickerView.OnDateSelectedListener
    {
        @Override
        public void onDateSelected(Date date) {
            first_date=date;
            int size = (calendarPickerView.getSelectedDates()).size();
            if (size >= 2) {
                dates.addAll(calendarPickerView.getSelectedDates());
                t_startDate.setText(getResources().getString(R.string.jobSart) + format.format(dates.get(0)));
                t_endDate.setText(getResources().getString(R.string.jobEnd) + format.format(dates.get(size - 1)));
                startTime = format2.format(dates.get(0));
                endTime = format2.format(dates.get(size - 1));
                dates.clear();
                popup_flag=false;
                first_date=null;
                hintButton.setText("请选择作业开始日期");
                gps_MachineLocation(machine_id);
                datePickerPop.dismiss();
            }
        }

        @Override
        public void onDateUnselected(Date date) {

        }
    }

    //ProgressDialog显示与隐藏
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }
    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

//    private class sl_areaClickListener implements AdapterView.OnItemClickListener
//    {
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//            sl_area = sp_area.getSelectedItem().toString();
//            switch (sl_area) {
//                case "50公里":
//                    sl_area = "50";
//                    break;
//                case "80公里":
//                    sl_area = "80";
//                    break;
//                case "100公里":
//                    sl_area = "100";
//                    break;
//                case "默认距离":
//                    sl_area = "50";
//                    break;
//            }
//            gps_MachineLocation(machine_id);
//        }
//    }
}
