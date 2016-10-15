package com.manhquan.extratraveltools.Modules;

import java.util.List;

/**
 * Created by user on 15/10/2016.
 */

public interface DirectionFinderListener {
    void onDirectionFinderStart();

    void onDirectionFinderSuccess(List<Route> route);
}
