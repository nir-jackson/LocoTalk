package il.co.nolife.locotalk;

/**
 * Created by Victor Belski on 9/8/2015.
 */
public interface IApiCallback<T> {

    public void Invoke(T result);

}
