package com.cookbook.fenix.cookbook;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by fenix on 05.07.2015.
 */
class Downloader extends Thread {

    public static final String TEST = "DOWNLOADER";
    public static final String TAG = "TEST_BMP";
    private Activity activity;
    public LinkedBlockingDeque<ImageDownloadTask> taskDeque = new LinkedBlockingDeque<ImageDownloadTask>();
    private ImageDownloadTask downloadTask;
    private RecipeAdapter recipeAdapter;
    private Recipe recipe;
    private boolean stop = true;


    Downloader  (Activity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public void run() {
        while(stop) {

            /*activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity.getApplicationContext(), "  " + Thread.currentThread().getId() + " = Downloader", Toast.LENGTH_SHORT).show();
                }
            });*/
            Log.d(TEST, " Thread num = " + Thread.currentThread().getId());



            if(stop&&(taskDeque.peekFirst()!=null)) {
                downloadTask = taskDeque.pollFirst();
                recipe = downloadTask.getRecipe();
                try {

                    URL url = new URL(recipe.getImgURL());
                    final Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    Log.d(TAG," height= "+bmp.getHeight()+" width = "+bmp.getWidth()+" size"+sizeOf(bmp));

                    recipe.setBmp(bmp);

                    activity.runOnUiThread(new Runnable() {
                        public void run() {

                            if(recipeAdapter != null) {
                                Integer i = downloadTask.getRecipeAdapterID();
                                try {
                                    if (i != null) {
                                        //CookBOOK.recipeAdapter.getItem(i);
                                        recipeAdapter.alterItem(recipe, i);
                                    }
                                } catch (IndexOutOfBoundsException e) {
                                    Log.e(TAG, "Index out of bounds = leaft 1 download task working");
                                    //CookBOOK.recipeList.remove(i);
                                    //CookBOOK.recipeList.set(i, recipe);
                                    e.printStackTrace();

                                }
                                RecipeFragment f = downloadTask.getRecipeFragment();
                                if (f != null) {
                                    f.setImage(bmp);
                                }
                            }
                        }
                    });

                    Log.d(TEST, recipe.getBmp().toString());
                    Log.d(TEST, "BMP download");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }else try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }
    public String sizeOf(Bitmap bmp){
       return String.valueOf(bmp.getHeight()*bmp.getWidth());
    }

    public void setStop(){
        this.stop = false;
    }

    public void setTaskDeque(LinkedBlockingDeque<ImageDownloadTask> deque){
        this.taskDeque = deque;
    }

    public LinkedBlockingDeque<ImageDownloadTask> getTaskDeque(){
        return taskDeque;
    }

    public void setLink(Activity a){
        this.activity = a;
    }

    public  void setRecipeAdapter(RecipeAdapter adapter){
        this.recipeAdapter=adapter;
    }

    public RecipeAdapter getRecipeAdapter(){
        return recipeAdapter;
    }

    public Activity getLink(){
        return activity;
    }
}



class ImageDownloadTask {
    private Recipe recipe;
    private Integer recipeAdapterID;
    private RecipeFragment recipeFragment;

    public ImageDownloadTask(Recipe r, Integer recipeAdapterID, RecipeFragment f){
        this.recipeAdapterID = recipeAdapterID;
        this.recipe = r;
        this.recipeFragment = f;

    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public Integer getRecipeAdapterID() {
        return recipeAdapterID;
    }

    public void setRecipeAdapterID(int recipeAdapterID) {
        this.recipeAdapterID = recipeAdapterID;
    }

    public RecipeFragment getRecipeFragment() {
        return recipeFragment;
    }

    public void setRecipeFragment(RecipeFragment recipeFragment) {
        this.recipeFragment = recipeFragment;
    }
}

