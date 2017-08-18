package com.yjprojects.jsctest2.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.yjprojects.jsctest2.R;
import com.yjprojects.jsctest2.recycler.BaseListClass;
import com.yjprojects.jsctest2.recycler.MainRecyclerViewAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recycler;
    private MainRecyclerViewAdapter adapter;
    private FloatingActionButton fab;
    private LinearLayoutManager glm;

    private List<BaseListClass> list = new ArrayList<>();




    private boolean close = false;
    //////////

    //////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initToolbar();
        initrecyclerview();
        new MediaAsyncTask().execute();

    }

    public void init(){
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close = true;
                finish();
            }
        });
    }

    private void initToolbar(){
        toolbar = (Toolbar) findViewById(R.id.mn_toolbar);
        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);
    }

    private void initrecyclerview() {
        recycler = (RecyclerView) findViewById(R.id.mn_recyclerView);
        glm = new LinearLayoutManager(this);
        recycler.setNestedScrollingEnabled(false);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(glm);
    }


    private void recyclerPatch(List<BaseListClass> list){
        adapter = new MainRecyclerViewAdapter(list);
        recycler.setAdapter(adapter);
    }

    public void onViewClicked(String id, BaseListClass data){
        //TODO : 클릭시 위치 맨 앞으로 변경
        Toast.makeText(this, data.getTitle(), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("name", data.getTitle());
        intent.putExtra("location", data.getId());


        startActivity(intent);
    }

    private void loadimage(){
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection, // Which columns to return
                    null,       // Return all rows
                    null,
                    null);


            int size = cursor.getCount();
            /*******  If size is 0, there are no images on the SD Card. *****/
            if (size == 0) {
            } else {
                while (cursor.moveToNext()) {

                    int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                    /**************** Captured image details ************/

                    /*****  Used to show image on view in LoadImagesFromSDCard class ******/
                    String path = cursor.getString(file_ColumnIndex);

                    String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());

                    int pos = fileName.lastIndexOf(".");
                    String ext = fileName.substring(pos+1);

                    Long ldate = new File(path).lastModified();
                    Date date = new Date(ldate);
                    BaseListClass mediaFileInfo = new BaseListClass(fileName, date, path);
                    list.add(mediaFileInfo);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MediaAsyncTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(String... params) {
            int result = 1;
            try {
                loadimage();
                Collections.sort(list, new Comparator<BaseListClass>() {
                    @Override
                    public int compare(BaseListClass o1, BaseListClass o2) {
                        return o2.getDate().compareTo(o1.getDate());
                    }
                });
            }catch (Exception e) {
                e.printStackTrace();
                result =0;
            }

            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {

            //첫 리스트 업데이트
            if (result == 1) {
                recyclerPatch(list);
            } else {
                Log.e("TAG", "Failed to fetch data!");
            }
        }
    }


    @Override
    public void onPause(){
        super.onPause();
        if(!close) {
            Intent pIntent = new Intent(MainActivity.this, ProjectionActivity.class);
            startActivity(pIntent);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }



}
