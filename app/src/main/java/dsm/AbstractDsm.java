package dsm;


import consistencyinfrastructure.architecture.IRegisterClient;

import java.io.Serializable;

/**
 * Created by Mio on 2015/12/21.
 */
public abstract class AbstractDsm<P extends Serializable, K extends Serializable, V extends Serializable> implements IRegisterClient<P, K, V> {

    public static final String TAG = AbstractDsm.class.getName();



    public abstract void onDestroy();
}
