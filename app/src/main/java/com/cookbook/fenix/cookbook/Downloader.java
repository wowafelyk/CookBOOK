package com.cookbook.fenix.cookbook;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**Downloads images in FixedThreadPool
 * All work happens is background threads.
 *
 * Created by fenix on 05.07.2015.
 */
public class Downloader extends Thread {

    public static final String TEST = "DOWNLOADER";
    public static final String TAG = "FixError";
    private static Bitmap placeholder;
    private WeakReference<Activity> mActivityWeakReference;
    public static LinkedBlockingDeque<ImageDownloadTask> taskDeque = new LinkedBlockingDeque<ImageDownloadTask>();
    private static LruCache<String, Bitmap> sMemoryCache;
    private RecipeAdapter recipeAdapter;
    private boolean stopPool = false;
    private boolean stop = true;
    private ExecutorService executor;


    Downloader(Activity activity) {
        super();
        mActivityWeakReference = new WeakReference<Activity>(activity);
        placeholder = BitmapFactory.decodeResource(activity.getResources(), R.drawable.placeholder);

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 6;
        sMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                if (Build.VERSION.SDK_INT >= 12) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TEST, " size= " + (bitmap.getByteCount() / 1024)
                                + " = " + (bitmap.getHeight() * bitmap.getWidth() * 4));
                    }
                    return bitmap.getByteCount() / 1024;
                } else {
                    return (bitmap.getHeight() * bitmap.getWidth() * 4 / 1024);
                }
            }
        };
    }


    @Override
    public void run() {
        executor = Executors.newFixedThreadPool(3);

        while (stop) {
            if (stopPool) {
                executor.shutdownNow();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                executor = Executors.newFixedThreadPool(3);

                stopPool(false);
            }


            if (taskDeque.size() >= 1) {
                while (!stopPool && (poolDownloadTask())) {

                }

            } else {
                    try {
                        Log.d(TEST, "Current thread = sleep ");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        executor.shutdown();


    }

    private boolean poolDownloadTask() {
        ImageDownloadTask downloadTask = taskDeque.poll();

        if (downloadTask != null) {
            executor.execute(new LoadTask(downloadTask));
            return true;
        }
        return false;
    }


    class LoadTask implements Runnable {
        private ImageDownloadTask ltDownloadTask;
        private Recipe ltRecipe;
        private Bitmap ltBmp;

        LoadTask(ImageDownloadTask task) {
            ltDownloadTask = task;
            ltRecipe = ltDownloadTask.getRecipe();
        }

        public void run() {

            if (!existsBitmap(ltRecipe.getImgURL())) {

                sMemoryCache.put(ltRecipe.getImgURL(), placeholder);
                InputStream is = null;
                URLConnection conn = null;
                ltBmp = null;

                try {
                    conn = new URL(ltRecipe.getImgURL()).openConnection();
                    is = conn.getInputStream();
                    ltBmp = BitmapFactory.decodeStream(is);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (is != null) try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (ltBmp != null) {
                    addBitmapToMemoryCache(ltRecipe.getImgURL(), ltBmp);
                } else {
                    sMemoryCache.remove(ltRecipe.getImgURL());
                }

                Activity a = mActivityWeakReference.get();
                try {
                    if (a != null && recipeAdapter != null) {
                        a.runOnUiThread(new Runnable() {
                            public void run() {
                                recipeAdapter.alterItem(ltDownloadTask.getPointer());
                            }
                        });
                    }
                } catch (NullPointerException e) {
                    Log.e(TAG, e.getMessage() + " (activity || adapter)==null while ORIENTATION change ");
                }
            }

            if (recipeAdapter == null)

            {
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Downloader crash - waiting adapter link");
                    e.printStackTrace();
                }
            }

            final ImageView v = ltDownloadTask.getImageView();
            if ((v != null) && (ltBmp != null)) {
                v.post(new Runnable() {
                    public void run() {
                        v.setImageBitmap(ltBmp);
                    }
                });
            }
            ImageDownloadTask ltDownloadTask = null;
            Recipe ltRecipe = null;
            Bitmap ltBmp = null;
        }


        private boolean existsBitmap(String key) {
            if (getBitmapFromMemCache(key) == null) {
                return false;
            }
            return true;
        }
    }

    //TODO: Add bitmap decoding for decrease using spase
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

    public void setStop(Boolean b) {
        this.stop = !b;
    }

    public void stopPool(Boolean b) {
        this.stopPool = b;
    }

    public void setTaskDeque(LinkedBlockingDeque<ImageDownloadTask> deque) {
        taskDeque = deque;
    }

    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            sMemoryCache.put(key, bitmap);
        } else if (!getBitmapFromMemCache(key).equals(bitmap))
            sMemoryCache.put(key, bitmap);
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        return sMemoryCache.get(key);
    }

    public static void setBitmapFromCache(ImageView imageView, Recipe r, Integer position, boolean b) {
        Bitmap bmp = sMemoryCache.get(r.getImgURL());
        if ((bmp != null) && (b == false)) {
            Log.d(TEST, "BMP1");
            if (bmp.equals(placeholder)) {
                Log.d(TEST, "BMP2");
                sMemoryCache.remove(r.getImgURL());
                addFirst(new ImageDownloadTask(r, position, imageView));
            } else imageView.setImageBitmap(bmp);
        } else if (bmp != null) {
            imageView.setImageBitmap(bmp);
        } else if ((bmp == null) && (b == true)) {
            imageView.setImageBitmap(placeholder);
            addLast(new ImageDownloadTask(r, position, imageView));
        } else if ((bmp == null) && (b == false)) {
            imageView.setImageBitmap(placeholder);
            addFirst(new ImageDownloadTask(r, position, imageView));
        }
    }

    private static void addFirst(ImageDownloadTask t) {
        new AsyncTask<ImageDownloadTask, Void, Void>() {

            @Override
            protected Void doInBackground(ImageDownloadTask... params) {
                taskDeque.addFirst(params[0]);
                return null;
            }

        }.execute(t);
    }

    private static void addLast(ImageDownloadTask t) {
        new AsyncTask<ImageDownloadTask, Void, Void>() {

            @Override
            protected Void doInBackground(ImageDownloadTask... params) {
                taskDeque.addLast(params[0]);
                return null;
            }

        }.execute(t);
    }


    //TODO: delete useless method

    public LinkedBlockingDeque<ImageDownloadTask> getTaskDeque() {
        return taskDeque;
    }

    public void setLink(Activity activity) {
        mActivityWeakReference = new WeakReference<Activity>(activity);
    }

    public void setRecipeAdapter(RecipeAdapter adapter) {
        this.recipeAdapter = adapter;
    }

    public RecipeAdapter getRecipeAdapter() {
        return recipeAdapter;
    }

    public WeakReference<Activity> getLink() {
        return mActivityWeakReference;
    }
}


class ImageDownloadTask {
    private Recipe recipe;
    private Integer pointer;
    private WeakReference<ImageView> mWeakImageView;
    public static final String TEST = "ImageDownloadTask";
    //private RecipeFragment recipeFragment;

    public ImageDownloadTask(Recipe r, Integer pointer, ImageView v) {
        this.pointer = pointer;
        this.recipe = r;
        this.mWeakImageView = new WeakReference<ImageView>(v);
        //Log.d(TEST, "Recipe0 =  " + recipe.hashCode() + " View = " + mWeakImageView.hashCode());
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public ImageView getImageView() {
        return mWeakImageView.get();
    }

    public void setImageView(ImageView ImageView) {
        this.mWeakImageView = new WeakReference<ImageView>(ImageView);
    }

    public Integer getPointer() {
        return pointer;
    }

    public void setPointer(Integer pointer) {
        this.pointer = pointer;
    }


}

