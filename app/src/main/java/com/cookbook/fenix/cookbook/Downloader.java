package com.cookbook.fenix.cookbook;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by fenix on 05.07.2015.
 */
class Downloader extends Thread {

    public static final String TEST = "DOWNLOADER";
    public static final String TAG = "FixError";
    private static Bitmap placeholder;
    private static Activity sActivity;
    public static LinkedBlockingDeque<ImageDownloadTask> taskDeque = new LinkedBlockingDeque<ImageDownloadTask>();
    private static LruCache<String, Bitmap> mMemoryCache;
    private ImageDownloadTask downloadTask;
    private RecipeAdapter recipeAdapter;
    private Recipe recipe;
    private boolean cancelTask = false;
    private boolean stop = true;
    private Bitmap bmp;
    private Integer mInfo[] = new Integer[4];


    Downloader(Activity activity) {
        super();
        sActivity = activity;
        placeholder = BitmapFactory.decodeResource(sActivity.getResources(), R.drawable.placeholder);


        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 2;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                if (Build.VERSION.SDK_INT >= 12) {
                    //if (BuildConfig.DEBUG) {
                    //    Log.d(TEST, " size= " + (bitmap.getByteCount() / 1024)
                    //           + " = " + (bitmap.getHeight() * bitmap.getWidth()));
                    //}
                    return bitmap.getByteCount() / 1024;
                } else {
                    return bitmap.getHeight() * bitmap.getWidth();
                }
            }
        };
    }


    @Override
    public void run() {
        while (stop) {

            /*activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity.getApplicationContext(), "  " + Thread.currentThread().getId() + " = Downloader", Toast.LENGTH_SHORT).show();
                }
            });*/

            Log.d(TAG, "size = " + taskDeque.size());
            if (stop && (taskDeque.peekFirst() != null)) {
                downloadTask = taskDeque.pollFirst();
                recipe = downloadTask.getRecipe();
                final Integer recipeAdapterID = downloadTask.getPointer();
                if (recipeAdapterID != null) saveCashInfo(recipeAdapterID);
                if (!existsBitmap(recipe.getImgURL())) {

                    InputStream is = null;
                    URL url = null;
                    URLConnection conn;
                    BufferedInputStream bis = null;
                    bmp = null;
                    try {
                        conn = new URL(recipe.getImgURL()).openConnection();
                        is = conn.getInputStream();
                        bmp = BitmapFactory.decodeStream(is);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (bis != null) try {
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (is != null) try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    //Log.d(TEST, " height= " + bmp.getHeight() + " width = " + bmp.getWidth() + " size" + sizeOf(bmp));
                    //Log.d(TEST, " height= " + bmp.getHeight() + " width = " + bmp.getWidth() + " size" + bmp.getByteCount());
                    if (bmp != null) {
                        addBitmapToMemoryCache(recipe.getImgURL(), bmp);
                        if (recipeAdapter == null) {
                            try {
                                Log.d(TEST, "Current thread = sleep ");
                                Thread.currentThread().sleep(1000);
                            } catch (InterruptedException e) {
                                Log.e(TAG, "Downloader crash - waiting adapter link");
                                e.printStackTrace();
                            }
                        } else {
                            sActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    recipeAdapter.alterItem(recipeAdapterID);
                                    final ImageView v = downloadTask.getImageView();
                                    if (v != null) {
                                        sActivity.runOnUiThread(new Runnable() {
                                            public void run() {
                                                v.setImageBitmap(bmp);
                                            }
                                        });

                                    }
                                }

                            });
                        }
                        //Log.d(TEST, "Current thread = " + Thread.currentThread().hashCode());
                    }
                }

            } else {
                Log.d(TEST, "info = " + mInfo[0] + " " + mInfo[1] + " " + mInfo[2] + " " + mInfo[3]);
                if (!cashNextImg()) {
                    try {
                        Log.d(TEST, "Current thread = sleep ");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

    }

    public String sizeOf(Bitmap bmp) {
        return String.valueOf(bmp.getHeight() * bmp.getWidth());
    }

    public static Bitmap decodeBitmapFromStream(BufferedInputStream stream, Rect outPadding, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(stream, outPadding, options);

        // Calculate inSampleSize
        options.inSampleSize = 2;//calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(stream, outPadding, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    //TODO: delete useless method
    public void setStop() {
        this.stop = false;
    }

    public void cancelTask() {
        this.cancelTask = true;
    }

    public void setTaskDeque(LinkedBlockingDeque<ImageDownloadTask> deque) {
        taskDeque = deque;
    }

    /**
     * mInfo
     *
     * @return
     */
    private boolean cashNextImg() {
        int i = RecipeAdapter.linkedList.size();
        if (mInfo[0] != null & mInfo[1] != null) {
            Log.d(TEST, "info3 = " + mInfo[0] + " " + mInfo[1] + " " + mInfo[2] + " " + mInfo[3] + " " + i);
            if (mInfo[2] == null || mInfo[3] == null) {
                mInfo[2] = mInfo[1];
                mInfo[3] = mInfo[1];
            }
            Log.d(TEST, "info2 = " + mInfo[0] + " " + mInfo[1] + " " + mInfo[2] + " " + mInfo[3]);
            if (mInfo[0] <= mInfo[1]) {
                //make cache for 15 bitmaps

                if ((mInfo[3] < i - 1) & (mInfo[3] - mInfo[1] < 20)) {
                    taskDeque.add(new
                            ImageDownloadTask(RecipeAdapter.linkedList.get(mInfo[3]++), null, null));
                    return true;
                } else if ((mInfo[2] > 0) & (mInfo[0] - mInfo[2] < 20)) {
                    taskDeque.add(new
                            ImageDownloadTask(RecipeAdapter.linkedList.get(mInfo[2]--), null, null));
                    return true;
                }
            } else if (mInfo[0] > mInfo[1]) {
                //make cache for 15 bitmaps
                if ((mInfo[2] > 0) & (mInfo[2] - mInfo[0] < 20)) {
                    taskDeque.add(new
                            ImageDownloadTask(RecipeAdapter.linkedList.get(mInfo[2]--), null, null));
                    return true;
                } else if ((mInfo[3] < i - 1) & (mInfo[3] - mInfo[1] < 20)) {
                    taskDeque.add(new
                            ImageDownloadTask(RecipeAdapter.linkedList.get(mInfo[3]++), null, null));
                    return true;
                }
            }
            return false;


        }
        return false;
    }

    private void saveCashInfo(Integer i) {
        mInfo[0] = mInfo[1];
        mInfo[1] = i;
        mInfo[2] = null;
        mInfo[3] = null;
    }


    private boolean existsBitmap(String key) {
        if (getBitmapFromMemCache(key) == null) {
            return false;
        } else if (getBitmapFromMemCache(key).equals(placeholder)) {
            return false;
        }


        return true;
    }

    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        } else if (!getBitmapFromMemCache(key).equals(bitmap))
            mMemoryCache.put(key, bitmap);
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public static void setBitmapFromCache(ImageView v, Recipe r, Integer position, boolean b) {
        Bitmap bmp = mMemoryCache.get(r.getImgURL());

        if (bmp == null) {
            addBitmapToMemoryCache(r.getImgURL(), placeholder);
            taskDeque.addLast(new ImageDownloadTask(r, position, v));
        } else if ((bmp == null) & (b == false)) {
            taskDeque.addFirst(new ImageDownloadTask(r, position, v));
        } else if (bmp != null) {
            v.setImageBitmap(bmp);
        }
    }
    //TODO: delete useless method

    public LinkedBlockingDeque<ImageDownloadTask> getTaskDeque() {
        return taskDeque;
    }

    public void setLink(Activity a) {
        sActivity = a;
    }

    public void setRecipeAdapter(RecipeAdapter adapter) {
        this.recipeAdapter = adapter;
    }

    public RecipeAdapter getRecipeAdapter() {
        return recipeAdapter;
    }

    public Activity getLink() {
        return sActivity;
    }
}


class ImageDownloadTask {
    private Recipe recipe;
    private Integer pointer;
    private ImageView imageView;
    //private RecipeFragment recipeFragment;

    public ImageDownloadTask(Recipe r, Integer pointer, ImageView v) {
        this.pointer = pointer;
        this.recipe = r;
        this.imageView = v;

    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public Integer getPointer() {
        return pointer;
    }

    public void setPointer(Integer pointer) {
        this.pointer = pointer;
    }


}

