package vn.pghh.thivangiaoly;

import android.app.Activity;
import android.os.Bundle;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final int CREAM = Color.rgb(255,248,235), BROWN = Color.rgb(101,53,32), GOLD = Color.rgb(230,185,112);
    private LinearLayout root, body, toolbar;
    private SQLiteDatabase db;
    private SharedPreferences prefs;
    private int fontSize, selectedGroup = 0;
    private String keyword = "";
    private boolean favoritesOnly = false;
    private int openId = -1;
    private String openTitle = "";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BROWN);
        getWindow().setNavigationBarColor(BROWN);
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        fontSize = prefs.getInt("font", 21);
        try { installDatabase(); db = SQLiteDatabase.openDatabase(getDatabasePath("thivan.sqlite").getPath(), null, SQLiteDatabase.OPEN_READONLY); }
        catch (Exception e) { showError("Không mở được dữ liệu: " + e.getMessage()); return; }
        showHome();
    }
    private void installDatabase() throws IOException {
        File dest = getDatabasePath("thivan.sqlite");
        if(dest.exists() && dest.length() > 0) return;
        if(!dest.getParentFile().exists() && !dest.getParentFile().mkdirs()) throw new IOException("Không tạo được thư mục dữ liệu");
        try(InputStream in = getAssets().open("thivan.sqlite"); OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[16384]; int n; while((n=in.read(buffer))!=-1) out.write(buffer,0,n);
        }
    }
    private int dp(float v){return (int)(getResources().getDisplayMetrics().density*v+0.5f);}
    private GradientDrawable shape(int color, int radius) {GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private TextView label(String s, int size, boolean bold, int color) {
        TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setGravity(Gravity.CENTER);v.setPadding(dp(10),dp(12),dp(10),dp(12));
        if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;
    }
    private void base(String title){
        root = new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(CREAM);
        setContentView(root);
        TextView header=label(title,23,true,BROWN);header.setPadding(dp(12),dp(20),dp(12),dp(14));root.addView(header);
        toolbar=new LinearLayout(this);toolbar.setOrientation(LinearLayout.HORIZONTAL);toolbar.setGravity(Gravity.CENTER);
        root.addView(toolbar);ScrollView sc=new ScrollView(this);sc.setFillViewport(true);
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(12),dp(8),dp(12),dp(22));sc.addView(body);
        root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
    }
    private void action(String s, Runnable run) {
        TextView v=label(s,15,true,BROWN);v.setBackground(shape(GOLD,12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,dp(46),1);lp.setMargins(dp(4),0,dp(4),dp(8));toolbar.addView(v,lp);
        v.setOnClickListener(w->run.run());
    }
    private void showHome(){
        openId=-1;base("THI VĂN GIÁO LÝ\nPHẬT GIÁO HÒA HẢO");
        action("Tất cả",()->{selectedGroup=0;favoritesOnly=false;keyword="";showHome();});
        action("Yêu thích",()->{favoritesOnly=true;selectedGroup=0;keyword="";showHome();});
        action("A−",()->{fontSize=Math.max(15,fontSize-2);prefs.edit().putInt("font",fontSize).apply();showHome();});
        action("A+",()->{fontSize=Math.min(34,fontSize+2);prefs.edit().putInt("font",fontSize).apply();showHome();});
        EditText search=new EditText(this);search.setSingleLine(true);search.setTextSize(17);search.setHint("Tìm theo tên hoặc nội dung bài viết...");
        search.setText(keyword);body.addView(search);
        LinearLayout category=new LinearLayout(this);category.setGravity(Gravity.CENTER_VERTICAL);
        Spinner spinner = new Spinner(this);
        ArrayList<String> groups = new ArrayList<>();groups.add("Tất cả 10 nhóm");for(int i=1;i<=10;i++)groups.add("Nhóm " + i);
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,groups);spinner.setAdapter(adapter);spinner.setSelection(selectedGroup);
        category.addView(spinner,new LinearLayout.LayoutParams(0,dp(54),1));
        TextView reset=label("Bỏ lọc",15,true,BROWN);category.addView(reset);
        reset.setOnClickListener(v->{selectedGroup=0;favoritesOnly=false;keyword="";showHome();});
        body.addView(category);
        LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);body.addView(list);
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> parent){}
            public void onItemSelected(android.widget.AdapterView<?> parent,View view,int pos,long id){selectedGroup=pos;renderList(list);}
        });
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int count,int after){}public void onTextChanged(CharSequence s,int st,int before,int count){keyword=s.toString();renderList(list);}public void afterTextChanged(Editable e){}});
        renderList(list);
    }
    private void renderList(LinearLayout list){
        list.removeAllViews();
        String where="1=1";ArrayList<String> args=new ArrayList<>();
        if(selectedGroup>0){where+=" AND idkhoa=?";args.add(String.valueOf(selectedGroup));}
        if(!keyword.trim().isEmpty()){where+=" AND (tieude LIKE ? OR noidung LIKE ?)";String q="%"+keyword.trim()+"%";args.add(q);args.add(q);}
        int count=0;
        try(Cursor c=db.query("thivangiaoly",new String[]{"_id","tieude","idkhoa"},where,args.toArray(new String[0]),null,null,"_id ASC")){
            while(c.moveToNext()){
                int id=c.getInt(0);String title=c.getString(1);int group=c.getInt(2);
                if(favoritesOnly && !prefs.getBoolean("f_"+id,false))continue;
                TextView item=label(title+"\n"+"Nhóm "+group+(prefs.getBoolean("f_"+id,false)?"  ★":""),fontSize,true,BROWN);
                item.setBackground(shape(Color.WHITE,12));item.setGravity(Gravity.CENTER);item.setPadding(dp(16),dp(16),dp(16),dp(16));
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(8));list.addView(item,lp);
                item.setOnClickListener(v->showArticle(id,title));count++;
            }
        }
        if(count==0)list.addView(label("Không tìm thấy bài viết phù hợp.",18,false,BROWN));
    }
    private void showArticle(int id,String title){
        openId=id;openTitle=title;base(title);
        action("← Danh sách",this::showHome);
        action(prefs.getBoolean("f_"+id,false)?"★ Đã lưu":"☆ Lưu bài",()->{prefs.edit().putBoolean("f_"+id,!prefs.getBoolean("f_"+id,false)).apply();showArticle(id,title);});
        action("A−",()->{fontSize=Math.max(15,fontSize-2);prefs.edit().putInt("font",fontSize).apply();showArticle(id,title);});
        action("A+",()->{fontSize=Math.min(34,fontSize+2);prefs.edit().putInt("font",fontSize).apply();showArticle(id,title);});
        try(Cursor c=db.query("thivangiaoly",new String[]{"noidung"},"_id=?",new String[]{String.valueOf(id)},null,null,null)){
            if(c.moveToFirst()){
                TextView t=label(c.getString(0),fontSize,false,BROWN);t.setGravity(Gravity.CENTER);t.setTextIsSelectable(true);
                t.setLineSpacing(dp(4),1.22f);t.setBackground(shape(Color.WHITE,12));t.setPadding(dp(18),dp(22),dp(18),dp(22));body.addView(t);
            }
        }
    }
    private void showError(String s){TextView v=label(s,18,false,Color.RED);setContentView(v);}
    @Override public void onBackPressed(){if(openId!=-1)showHome();else super.onBackPressed();}
    @Override protected void onDestroy(){if(db!=null)db.close();super.onDestroy();}
}
