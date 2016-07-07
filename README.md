# CachedObject
Cached all object with better performance

#How to use

init an instance of CachedUtils
```
CachedUtils cachedUtils = new CachedUtils();
```

#Save a object
```
ArrayList<HomeBanner> homeBannerList = ...;
```
With HomeBanner class
```
class HomeBanner implements CachedUtils.ICacheable

    @Override
    public byte[] toBytes() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStreamWriter = null;
        try {
            outputStreamWriter = new ObjectOutputStream(byteArrayOutputStream);
            outputStreamWriter.writeUTF(mId);
            outputStreamWriter.writeUTF(mThumbnail);
            outputStreamWriter.writeUTF(mObjectType);
            outputStreamWriter.close();

        } catch (IOException e) {
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public CachedUtils.ICacheable fromBytes(byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = null;
        try {
            HomeBanner homeBanner = new HomeBanner();
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            homeBanner.mId = objectInputStream.readUTF();
            homeBanner.mThumbnail = objectInputStream.readUTF();
            homeBanner.mObjectType = objectInputStream.readUTF();
            objectInputStream.close();
            return homeBanner;
        } catch (IOException e) {
        }
        return this;
    }

```

call save method
```
cachedUtils.saveData("method_name_you_want", homeBannerList, null, CachedUtils.CACHED_TYPE_FOREVER);
```

These are 3 cached type you can use
```
static public final long CACHED_TYPE_READ_ONCE = -1L;
static public final long CACHED_TYPE_FOREVER = 0L;
or
define a time as you need in millisecond
```

#Load a object arrays
```
cachedUtils.loadData("method_name_you_want", HomeBanner.class, new CachedUtils.ICachedLoaded<HomeBanner>() {
                    @Override
                    public void onFinish(final ArrayList<HomeBanner> iData) {
                       //handle your arraylist
                    }
                });
```

