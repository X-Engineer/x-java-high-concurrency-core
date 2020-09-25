package com.crazymakercircle.completableFutureDemo;

import com.crazymakercircle.util.Print;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Case
{
    ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Test
    public void thenApply() throws Exception
    {

        CompletableFuture cf = CompletableFuture.supplyAsync(() ->
        {
            try
            {
                //休眠2秒
                Thread.sleep(2000);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            Print.tcfo("supplyAsync " + Thread.currentThread().getName());
            return "hello ";
        }, executorService).thenAccept(s ->
        {
            try
            {
                Print.tcfo("thenApply_test" + s + "world");
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        Print.tcfo(Thread.currentThread().getName());
        while (true)
        {
            if (cf.isDone())
            {
                Print.tcfo("CompletedFuture...isDown");
                break;
            }
        }
    }

}
