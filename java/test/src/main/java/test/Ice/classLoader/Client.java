// **********************************************************************
//
// Copyright (c) 2003-2016 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

package test.Ice.classLoader;

public class Client extends test.Util.Application
{
    @Override
    public int run(String[] args)
    {
        AllTests.allTests(this, false);
        return 0;
    }

    @Override
    protected GetInitDataResult getInitData(String[] args)
    {
        GetInitDataResult r = super.getInitData(args);
        _initData = r.initData;
        _initData.properties.setProperty("Ice.Package.Test", "test.Ice.classLoader");
        return r;
    }

    public static void main(String[] args)
    {
        Client app = new Client();
        int result = app.main("Client", args);
        System.gc();
        System.exit(result);
    }

    private com.zeroc.Ice.InitializationData _initData;
}
