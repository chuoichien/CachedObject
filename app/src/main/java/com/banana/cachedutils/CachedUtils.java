package com.banana.cachedutils;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Pair;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by Thanhlnh on 6/29/2016.
 */
public class CachedUtils {
    static public final long CACHED_TYPE_READ_ONCE = -1L;
    static public final long CACHED_TYPE_FOREVER = 0L;

    static public final int CACHED_VERSION = 1;

    static private final int SAVE_CACHED = 1;
    static private final int LOAD_CACHED = 2;

    public <T extends ICacheable> void saveData(Context context, String method, ArrayList<T> listHome, ICachedLoaded<T> iCachedLoaded, long cachedTime) {
        if (listHome != null && !listHome.isEmpty()) {
            CachedWorkerAsync<T> workerAsync = new CachedWorkerAsync<>(context, null, iCachedLoaded, cachedTime);
            Pair<String, ArrayList<T>> inputData = new Pair<>(method, listHome);
            Pair<Integer, Pair<String, ArrayList<T>>> actionTask = new Pair<>(SAVE_CACHED, inputData);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
                workerAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, actionTask);
            } else {
                workerAsync.execute(actionTask);
            }
        }
    }

    public <T extends ICacheable> void loadData(Context context, String method, Class<T> castClass, ICachedLoaded<T> iCachedLoaded) {
        if (castClass != null) {
            CachedWorkerAsync<T> workerAsync = new CachedWorkerAsync<>(context, castClass, iCachedLoaded);
            Pair<String, ArrayList<T>> inputData = new Pair<>(method, null);
            Pair<Integer, Pair<String, ArrayList<T>>> actionTask = new Pair<>(LOAD_CACHED, inputData);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
                workerAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, actionTask);
            } else {
                workerAsync.execute(actionTask);
            }
        }
    }

    public interface ICacheable<T extends ICacheable> {
        byte[] toBytes();

        T fromBytes(byte[] bytes);
    }

    public interface ICachedLoaded<T> {
        void onFinish(ArrayList<T> iData);
    }

    private static class CachedWorkerAsync<T extends ICacheable> extends AsyncTask<Pair<Integer, Pair<String, ArrayList<T>>>, Void, ArrayList<T>> {

        private ICachedLoaded<T> cachedLoaded;
        private Class<T> castClass;
        private long cachedTime;
        private Context mContext;

        public CachedWorkerAsync(Context context, Class<T> castClass, ICachedLoaded<T> cachedLoaded, long cachedTime) {
            this.mContext = context;
            this.cachedLoaded = cachedLoaded;
            this.castClass = castClass;
            this.cachedTime = cachedTime;
        }

        public CachedWorkerAsync(Context context, Class<T> castClass, ICachedLoaded<T> cachedLoaded) {
            this.mContext = context;
            this.cachedLoaded = cachedLoaded;
            this.castClass = castClass;
        }

        @Override
        protected ArrayList<T> doInBackground(Pair<Integer, Pair<String, ArrayList<T>>>... params) {
            if (params != null && params.length > 0) {
                Pair<Integer, Pair<String, ArrayList<T>>> taskAction = params[0];
                if (taskAction != null) {
                    Pair<String, ArrayList<T>> iData = taskAction.second;
                    String method = iData.first;
                    ArrayList<T> dataList = iData.second;

                    switch (taskAction.first) {
                        case LOAD_CACHED:
                            final ArrayList<T> list = loadData(method);
                            if (cachedLoaded != null) {
                                cachedLoaded.onFinish(list);
                            }
                            break;
                        case SAVE_CACHED:
                            final ArrayList<T> arrayList = saveData(method, dataList);
                            if (cachedLoaded != null) {
                                cachedLoaded.onFinish(arrayList);
                            }
                            break;
                    }
                }
            }
            return null;
        }

        private File getFileSaveLocation(String method) throws IOException {
            if (!TextUtils.isEmpty(method)) {
                method = method.toLowerCase();
            }
            File file = new File(Constants.CACHED_DIRECTORY);
            if (!file.exists()) {
                file.mkdirs();
            }

            File oldFile = getFileLoadLocation(method);
            if (oldFile != null) {
                return oldFile;
            } else {
                String filePath = file.getAbsolutePath() + File.separator + "home_cached";
                if (!TextUtils.isEmpty(method)) {
                    filePath += "_" + method;
                    SharedPrefrenencesManager.setStringValue(mContext, SharedPrefrenencesManager.CACHED_HOME_DATA + method, filePath);
                } else {
                    SharedPrefrenencesManager.setStringValue(mContext, SharedPrefrenencesManager.CACHED_HOME_DATA, filePath);
                }
                return new File(filePath);
            }
        }

        private File getFileLoadLocation(String method) throws IOException {
            if (!TextUtils.isEmpty(method)) {
                method = method.toLowerCase();
            }
            String filePath = SharedPrefrenencesManager.getStringValue(mContext, SharedPrefrenencesManager.CACHED_HOME_DATA + method, "");
            if (!TextUtils.isEmpty(filePath)) {
                return new File(filePath);
            }
            return null;
        }

        public ArrayList<T> saveData(String method, ArrayList<T> listHome) {
            synchronized (CachedUtils.class) {
                if (listHome != null && !listHome.isEmpty()) {
                    FileOutputStream fileOutputStream = null;
                    ObjectOutputStream objectOutputStream = null;
                    try {
                        //Do not use objectOutputStream.writeObjects(...); to slow performance
                        File fileSaved = getFileSaveLocation(method);
                        if (fileSaved.exists()) {
                            fileSaved.delete();
                        }
                        fileOutputStream = new FileOutputStream(fileSaved);
                        objectOutputStream = new ObjectOutputStream(fileOutputStream);

                        objectOutputStream.writeInt(CACHED_VERSION);
                        objectOutputStream.writeLong(System.currentTimeMillis());
                        objectOutputStream.writeLong(cachedTime);

                        int size = listHome.size();
                        objectOutputStream.writeInt(size);
                        for (T item : listHome) {
                            byte[] bytes = item.toBytes();
                            objectOutputStream.writeInt(bytes.length);
                            objectOutputStream.write(bytes);
                            objectOutputStream.flush();
                        }
                        fileOutputStream.flush();
                        return listHome;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    } finally {
                        silentCloseStream(objectOutputStream);
                        silentCloseStream(fileOutputStream);
                    }
                }
                return null;
            }
        }

        private T createClass(Class<T> clazz) {
            try {
                return clazz.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }

        private ArrayList<T> loadData(String method) {
            if (castClass == null) {
                return null;
            }
            synchronized (CachedUtils.class) {
                FileInputStream fileInputStream = null;
                ObjectInputStream objectInputStream = null;
                boolean clearFileOnCompleted = false;
                File fileSaved = null;
                try {
                    fileSaved = getFileLoadLocation(method);
                    if (fileSaved != null) {
                        fileInputStream = new FileInputStream(fileSaved);
                        objectInputStream = new ObjectInputStream(fileInputStream);
                        int version = objectInputStream.readInt();
                        if (version != CACHED_VERSION) {
                            clearFileOnCompleted = true;
                            return null;
                        }

                        long cachedSavedTime = objectInputStream.readLong();
                        long cachedTime = objectInputStream.readLong();
                        if(cachedTime == CachedUtils.CACHED_TYPE_READ_ONCE) {
                            clearFileOnCompleted = true;
                        } else if (cachedTime == CachedUtils.CACHED_TYPE_FOREVER) {
                            //nothing to do
                        } else if(System.currentTimeMillis() - cachedSavedTime > cachedTime) {
                            clearFileOnCompleted = true;
                            return null;
                        }

                        int size = objectInputStream.readInt();
                        if (size > 0) {
                            ArrayList<T> outData = new ArrayList<>(size);
                            for (int i = 0; i < size; i++) {
                                int length = objectInputStream.readInt();
                                byte[] bytes = new byte[length];
                                int read = objectInputStream.read(bytes);
                                if (read != -1) {
                                    T object = createClass(castClass);
                                    if (object != null) {
                                        object.fromBytes(bytes);
                                        outData.add(object);
                                    }
                                }
                                objectInputStream.skipBytes(length - read);
                            }
                            return outData;
                        }
                    }
                } catch (Exception e) {

                } finally {
                    silentCloseStream(fileInputStream);
                    silentCloseStream(objectInputStream);
                    if(clearFileOnCompleted && fileSaved != null) {
                        try {
                            fileSaved.delete();
                            SharedPrefrenencesManager.setStringValue(mContext, SharedPrefrenencesManager.CACHED_HOME_DATA + method, "");
                        } catch (Exception e) {

                        }
                    }
                }
                return null;
            }
        }

        private void silentCloseStream(Closeable stream) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
