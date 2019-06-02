package com.artk.gallery.api;

/**
 * This class manages DataProvider objects.
 * To get a new DataProvider object, call create method
 */
public class DataProviders {

    /**
     * Here we decide which DataProvider to return
     * We can replace production data with test data
     */
    public static DataProvider create(DataProvider.Callback callback) {
        return production(callback);
    }

    // real data provider
    private static DataProvider production(DataProvider.Callback callback) {
        return new RealDataProvider(callback);
    }

    // test provider
    private static DataProvider test(DataProvider.Callback callback) {
        return new TestDataProvider(callback);
    }

}
