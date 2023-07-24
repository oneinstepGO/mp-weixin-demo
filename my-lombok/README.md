# my-lombok

lombok 示例

公众号文章地址：
[java注解处理器](https://mp.weixin.qq.com/s/gscNRiLrCQxA0ahcFuxGYA)

1、打包

```
maven clean install
```

2、引入

```
<dependency>
    <groupId>com.lombok</groupId>
    <artifactId>my-lombok</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

3、使用

```
import com.oneinstep.apt.MyGetter;
@MyGetter
public class User {
    private String name;
    private Integer age;
}
```