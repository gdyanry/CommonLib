## 概述
* 本库可用于Java客户端和服务端开发，也可以作为[AndroidLib](https://github.com/gdyanry/AndroidLib)的依赖库用于Android开发。
* 相关文档会陆续补充。

## 开发层通信协议

所谓开发层通信协议，是指对底层数据传输实现进行抽象之后，对字符数据格式作出某些约定，以较小的成本实现期望的功能。

本文所提出的协议，主要是基于移动网络的不稳定性保证数据交换的可靠性，并支持推送扩展。

客户端发往服务器的文本格式为：

```json
[SESSION_ID, [[FLAG, REQUEST_ID, {TAG:[[INNER_ID, PARAM], ...], ...}], ...]]
```

> - SESSION_ID是客户端登录或注册时由服务器生成的唯一字符串，保存在客户端，用于标识用户身份。
> - FLAG为整数，用于标识请求和响应。0表示响应，大于0表示请求，并用位标识表示请求类型。其中1表示普通请求；2表示如果发生请求时无网络连接，则该请求会被缓存到内存直到网络可用时发送；4表示如果请求发送失败，则该请求会被缓存到内存并随其后的请求一起发送，直到成功为止；8表示如果请求发送失败，则该请求会被缓存到本地并随其后的请求一起发送，直到成功为止。
> - REQUEST_ID用于标识同一客户端的不同请求，可以基于时间戳生成。
> - TAG为api标识。
> - INNER_ID用于标识同一TAG中不同的参数，可以是基于当前TAG的计数器。
> - PARAM为可扩展的请求参数。

服务器的响应文本基本格式为：

```json
[[FLAG, REQUEST_ID, {TAG:[[INNER_ID, DATA], ...], ...}], ...]
```

> 具体项目可对DATA及外层进行扩展，以添加响应码。

## HTTP

Http请求使用同步方式，开发人员需要根据需要自己处理线程问题。

### 请求

#### GET

```java
HttpResponse resp = Https.get(baseUrl, params);
```

如果需要支持断点续传，则使用重载函数：

```java
HttpResponse resp = Https.get(baseUrl, params, startPos);
```

#### POST

```java
HttpResponse resp = Https.post(baseUrl, params, entity);
```

> * entity可以为byte[]或者InputStream。

如果需要干预上传过程，则需要提供StreamTransferHook对象：

```java
StreamTransferHook hook = new StreamTransferHook() {

  @Override
  public void onUpdate(long transferedBytes) {
    // 已传输字节数
  }

  @Override
  public void onFinish(boolean isStopped) {
    // 传输结束，也有可能是被中断
  }

  @Override
  public boolean isStop() {
    // 可以通过返回true来中断传输
    return false;
  }

  @Override
  public int getUpdateInterval() {
    // 更新进度的时间间隔，单位为毫秒
    return 250;
  }

  @Override
  public int getBufferSize() {
    // 传输缓存区大小，若返回0则使用默认值4096
    return 0;
  }
};
HttpResponse resp = Https.post(baseUrl, params, entity, hook);
```

使用表单上传：

```java
MultipartForm form = new MultipartForm(url, charset, uploadHook);
// 添加文本域
form.addText(name, value);
// 添加文件域
form.addFile(name, file);
// 添加字节数组或流
form.addBytes(fieldName, bytes, fileName);
form.addStream(fieldName, in, fileName);
// 获取响应对象
HttpResponse resp = form.getResponse();
```

自定义表单连接参数：

```java
MultipartForm form = new MultipartForm(url, charset, uploadHook) {
  @Override
  public void customize(HttpURLConnection conn) {
    // 可在此处设置连接参数
  }
};
```

#### 自定义连接

以上是常用的http请求方式，如果确实没有你想要的，可以使用自定义连接。

```java
CustomRequest request = new CustomRequest() {
  @Override
  public void customize(HttpURLConnection conn) {
    // 可在此处设置连接参数
  }
};
HttpResponse resp = Https.customizedRequest(baseUrl, params, request);
```

### 使用加密协议

如果使用私有证书的https协议，需要在进程初始化的时候调用：

```java
Https.initSSL(verifier, certificates, clientKey, password);
```

> * verifier指定可访问的主机名，若不匹配则拒绝连接，可为null。
> * certificates为字典结构，用于指定服务器证书。
> * clientKey为客户端证书，在双向验证的场景中才需要此参数。
> * password为客户端证书密码，在双向验证的场景中才需要此参数。

### 响应

响应的处理是通过HttpResponse对象来完成。处理响应前需要先调用`resp.isSuccess()`判断响应是否成功；可以调用`resp.getTotalLength()`查看响应内容的大小。

获取文本：

```java
String text = resp.getString(charset);
```

将响应保存到文件：

```java
IOUtil.transferStream(resp.getConnection().getInputStream(), new FileOutputStream(file));
```

如果需要干预下载过程，则使用重载函数：

```java
IOUtil.transferStream(resp.getConnection().getInputStream(), outStream, hook);
```

> * 此处hook与干预上传过程所用的hook为同一接口，用法可参考前面。

处理完成后，如果需要，可以调用`resp.getElapsedTimeMillis()`查看请求耗时。

## 资源访问

### 模型

根据key访问某资源，若资源不存在则执行生成资源。API使用请参考CacheResourceAccess和AccessHook的类文档。

### 扩展示例——根据URL访问文件

以URL作为key从本地缓存访问文件，若文件不存在则下载文件并保存在本地缓存。

```java
// 定义从url到本地路径的匹配转换器
FileHashMapper mapper = new FileHashMapper(CommonUtils.getDiskCacheDir(context), 10, "demo");
// 定义资源访问器
UrlFileAccess access = new UrlFileAccess(mapper) {

  @Override
  protected Executor getGenerationExecutor() {
    // 返回执行生成资源（即下载文件）所使用的线程池，若不需要切换线程则返回null
    return Singletons.get(DefaultExecutor.class);
  }

  @Override
  protected boolean supportResume() {
    // 若支持断点续传下载则返回true
  }
};
// 定义资源访问请求
UrlFileRequest request = new UrlFileRequest() {

  @Override
  public void onGenerateException(Exception e) {
    // 下载文件过程的异常回调
  }

  @Override
  public int getUpdateInterval() {
    // 返回下载进度更新回调触发的最小时间间隔，以毫秒为单位
    return 300;
  }

  @Override
  protected void onUpdate(long currentPos, long totalLen) {
    // 下载进度更新回调
  }

  @Override
  protected void onFileAvailable(File file) {
    // 成功访问文件回调
  }
};
// 根据url访问文件
request.start(access, url);
```

以上是把下载的内容保存为本地文件，如果要保存到数据库，则可以使用UrlBytesAccess，用法类似，不再详述。

最后需要强调的一点是，CacheResourceAccess是一种以缓存访问资源的方式的抽象定义，具有很高的扩展性。本库中的图片加载框架的核心实现就是通过对CacheResourceAccess的多重扩展以及组合来完成的。

