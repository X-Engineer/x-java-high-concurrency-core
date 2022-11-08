package com.crazymakercircle.jvm;

import com.crazymakercircle.util.Print;
import net.openhft.affinity.AffinityLock;
import org.junit.Test;
import org.openjdk.jol.info.ClassLayout;

public class ObjectTest {


    @Test
    public void testEmptyString() throws InterruptedException {

        String s=  new String();
        String printable = ClassLayout.parseInstance(s).toPrintable();
        Print.tcfo("printable = " + printable);
        }


    @Test
    public void testNotEmptyString() throws InterruptedException {

        String s=  new String("100000000000AAAAAAAAAA");
        String printable = ClassLayout.parseInstance(s).toPrintable();
        Print.tcfo("printable = " + printable);
        }
    }

