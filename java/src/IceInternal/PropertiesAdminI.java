// **********************************************************************
//
// Copyright (c) 2003-2012 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

package IceInternal;

public class PropertiesAdminI extends Ice._PropertiesAdminDisp implements Ice.NativePropertiesAdmin
{
    public PropertiesAdminI(String name, Ice.Properties properties, Ice.Logger logger)
    {
        _name = name;
        _properties = properties;
        _logger = logger;
    }

    public synchronized String
    getProperty(String name, Ice.Current current)
    {
        return _properties.getProperty(name);
    }

    public synchronized java.util.TreeMap<String, String>
    getPropertiesForPrefix(String name, Ice.Current current)
    {
        return new java.util.TreeMap<String, String>(_properties.getPropertiesForPrefix(name));
    }

    public void
    setProperties_async(Ice.AMD_PropertiesAdmin_setProperties cb, java.util.Map<String, String> props,
                        Ice.Current current)
    {
        java.util.Map<String, String> added = new java.util.HashMap<String, String>();
        java.util.Map<String, String> changed = new java.util.HashMap<String, String>();
        java.util.Map<String, String> removed = new java.util.HashMap<String, String>();
        Ice.PropertiesAdminUpdateCallback callback;
        
        synchronized(this)
        {
            java.util.Map<String, String> old = _properties.getPropertiesForPrefix("");
            final int traceLevel = _properties.getPropertyAsInt("Ice.Trace.Admin.Properties");

            //
            // Compute the difference between the new property set and the existing property set:
            //
            // 1) Any properties in the new set that were not defined in the existing set.
            //
            // 2) Any properties that appear in both sets but with different values.
            //
            // 3) Any properties not present in the new set but present in the existing set.
            //    In other words, the property has been removed.
            //
            for(java.util.Map.Entry<String, String> e : props.entrySet())
            {
                final String key = e.getKey();
                final String value = e.getValue();
                if(!old.containsKey(key))
                {
                    if(value.length() > 0)
                    {
                        //
                        // This property is new.
                        //
                        added.put(key, value);
                    }
                }
                else
                {
                    if(!value.equals(old.get(key)))
                    {
                        if(value.length() == 0)
                        {
                            //
                            // This property was removed.
                            //
                            removed.put(key, value);
                        }
                        else
                        {
                            //
                            // This property has changed.
                            //
                            changed.put(key, value);
                        }
                    }

                    old.remove(key);
                }
            }

            if(traceLevel > 0 && (!added.isEmpty() || !changed.isEmpty() || !removed.isEmpty()))
            {
                StringBuilder out = new StringBuilder(128);
                out.append("Summary of property changes");

                if(!added.isEmpty())
                {
                    out.append("\nNew properties:");
                    for(java.util.Map.Entry<String, String> e : added.entrySet())
                    {
                        out.append("\n  ");
                        out.append(e.getKey());
                        if(traceLevel > 1)
                        {
                            out.append(" = ");
                            out.append(e.getValue());
                        }
                    }
                }

                if(!changed.isEmpty())
                {
                    out.append("\nChanged properties:");
                    for(java.util.Map.Entry<String, String> e : changed.entrySet())
                    {
                        out.append("\n  ");
                        out.append(e.getKey());
                        if(traceLevel > 1)
                        {
                            out.append(" = ");
                            out.append(e.getValue());
                            out.append(" (old value = ");
                            out.append(_properties.getProperty(e.getKey()));
                            out.append(")");
                        }
                    }
                }

                if(!removed.isEmpty())
                {
                    out.append("\nRemoved properties:");
                    for(java.util.Map.Entry<String, String> e : removed.entrySet())
                    {
                        out.append("\n  ");
                        out.append(e.getKey());
                    }
                }

                _logger.trace(_name, out.toString());
            }

            //
            // Update the property set.
            //

            for(java.util.Map.Entry<String, String> e : added.entrySet())
            {
                _properties.setProperty(e.getKey(), e.getValue());
            }

            for(java.util.Map.Entry<String, String> e : changed.entrySet())
            {
                _properties.setProperty(e.getKey(), e.getValue());
            }

            for(java.util.Map.Entry<String, String> e : removed.entrySet())
            {
                _properties.setProperty(e.getKey(), "");
            }

            callback = _updateCallback;
        }

        //
        // Send the response now so that we do not block the client during the call to the update callback.
        //
        cb.ice_response();

        if(callback != null)
        {
            java.util.Map<String, String> changes = new java.util.HashMap<String, String>(added);
            changes.putAll(changed);
            changes.putAll(removed);

            try
            {
                callback.updated(changes);
            }
            catch(RuntimeException ex)
            {
                // Ignore.
            }
        }
    }

    public synchronized void
    setUpdateCallback(Ice.PropertiesAdminUpdateCallback cb)
    {
        _updateCallback = cb;
    }

    private final String _name;
    private final Ice.Properties _properties;
    private final Ice.Logger _logger;
    private Ice.PropertiesAdminUpdateCallback _updateCallback;
}
