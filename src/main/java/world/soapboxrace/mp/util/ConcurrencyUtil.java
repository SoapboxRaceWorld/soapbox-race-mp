package world.soapboxrace.mp.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ConcurrencyUtil
{
    public static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(16);
}
