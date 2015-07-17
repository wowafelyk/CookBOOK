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
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReentrantLock;

/**
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
    private Recipe recipe;
    private boolean stopPool = true;
    private boolean stop = true;
    private ReentrantLock lock = new ReentrantLock();
    Runnable task;
    private Bitmap bmp;
    //private Integer mInfo[] = new Integer[4];
    private ExecutorService executor;


    Downloader(Activity activity) {
        super();
        mActivityWeakReference = new WeakReference<Activity>(activity);
        placeholder = BitmapFactory.decodeResource(activity.getResources(), R.drawable.placeholder);


        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 5;
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

        executor = Executors.newFixedThreadPool(5);
        while (stop) {
            Log.d(TEST, "Current thread LruCache=  " + sMemoryCache.size());
            Log.d(TEST, "Current thread =  " + Thread.currentThread().hashCode());
            Log.d(TEST, "maxMemory =  " + Runtime.getRuntime().maxMemory() / 1024);
            Log.d(TAG, "size = " + taskDeque.size());
            //downloadTask = taskDeque.poll();
            //if (downloadTask = null) {
            //new Thread(new LoadTask()).start();
            if (taskDeque.size() >= 1) {
                while (stopPool && (poolDownloadTask())) {
                }
                //executor.shutdown();
            } else {
                //Log.d(TEST, "info = " + mInfo[0] + " " + mInfo[1] + " " + mInfo[2] + " " + mInfo[3]);
                //if (!cachNextImg()) {
                if (true) {   //!cachNextImg()
                    try {
                        Log.d(TEST, "Current thread = sleep ");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        executor.shutdown();
    }

    private synchronized boolean poolDownloadTask() {
        ImageDownloadTask downloadTask = taskDeque.poll();
        //Log.d(TEST, "Recipe2 = Task  " + downloadTask.hashCode());
        //Log.d(TEST, "Recipe2 = View  " + downloadTask.getmWeakImageView().hashCode());
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

            // Log.d(TEST, "Recipe2 = Task " + ltDownloadTask.hashCode());
            //Log.d(TEST, "Recipe2 = ltRecipe " + ltRecipe.hashCode());
            //Log.d(TEST, "Recipe2 =  " + ltRecipe.hashCode() + " View = " + ltDownloadTask.getmWeakImageView().hashCode());

        }


        public void run() {

            Log.d(TEST, "Thread1 =  " + Thread.currentThread().hashCode());

            //TODO: Change caching not viewed images
            //if (recipeAdapterID != null)
            //    saveCashInfo(recipeAdapterID);
            if (!existsBitmap(ltRecipe.getImgURL())) {
                //Log.d(TEST, "Recipe3 =  " + recipe.hashCode() + " View = " + ltDownloadTask.getmWeakImageView().hashCode());
                sMemoryCache.put(ltRecipe.getImgURL(), placeholder);
                InputStream is = null;
                URLConnection conn;
                //BufferedInputStream bis = null;
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
                    /*if (bis != null) try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
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
                                recipeAdapter.alterItem();
                            }
                        });
                    }
                } catch (NullPointerException e) {
                    Log.d(TAG, e.getMessage() + " (activity || adapter)==null while ORIENTATION change ");
                }
            }

            if (recipeAdapter == null)

            {
                try {
                    Log.d(TEST, "Current thread = sleep wait adapter init ");
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
                        //Log.d(TEST, "Recipe3 =  " + ltRecipe.hashCode() + " View = " + v.hashCode());
                        recipeAdapter.alterItem();
                        v.setImageBitmap(ltBmp);
                    }
                });
            }
        }

        /*private void saveCashInfo(Integer i) {
            lock.lock();
            try {
                mInfo[0] = mInfo[1];
                mInfo[1] = i;
                mInfo[2] = null;
                mInfo[3] = null;
            } finally {
                lock.unlock();
            }
        }*/

        private boolean existsBitmap(String key) {
            if (getBitmapFromMemCache(key) == null) {
                return false;
            } //else if (getBitmapFromMemCache(key).equals(placeholder)) {
            //return false;
            //}
            return true;
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

    /**
     * if b set true thread Downloader stoped
     *
     * @param b
     */
    public void setStop(Boolean b) {
        this.stop = !b;
    }

    /**
     * if b set true thread Downloader stoped
     *
     * @param b
     */
    public void stopPool(Boolean b) {
        this.stopPool = !b;
    }

    public void setTaskDeque(LinkedBlockingDeque<ImageDownloadTask> deque) {
        taskDeque = deque;
    }

    /*private synchronized boolean cachNextImg() {
        int i = RecipeAdapter.linkedList.size();
        lock.lock();
        try {
            if (mInfo[0] != null & mInfo[1] != null) {
                Log.d(TEST, "info3 = " + mInfo[0] + " " + mInfo[1] + " " + mInfo[2] + " " + mInfo[3] + " " + i);
                if ((mInfo[2] == null) || (mInfo[3] == null)) {
                    mInfo[2] = mInfo[1];
                    mInfo[3] = mInfo[1];
                }
                Log.d(TEST, "info2 = " + mInfo[0] + " " + mInfo[1] + " " + mInfo[2] + " " + mInfo[3]);
                if (mInfo[0] <= mInfo[1]) {
                    //make cache for 15 bitmaps

                    if ((mInfo[3] < i - 1) && (mInfo[3] - mInfo[1] < 20)) {
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
        } finally {
            lock.unlock();
        }
    }*/

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
        if (bmp != null) {
            imageView.setImageBitmap(bmp);
        } else if ((bmp == null) && (b == true)) {
            imageView.setImageBitmap(placeholder);
            taskDeque.addLast(new ImageDownloadTask(r, position, imageView));
        } else if ((bmp == null) && (b == false)) {
            imageView.setImageBitmap(placeholder);
            taskDeque.addFirst(new ImageDownloadTask(r, position, imageView));
        }
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

