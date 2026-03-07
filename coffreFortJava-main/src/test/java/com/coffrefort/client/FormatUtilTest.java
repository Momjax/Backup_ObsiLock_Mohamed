package com.coffrefort.client;

import com.coffrefort.client.util.FileUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FormatUtilTest {

    @Test
    public void testFormatFileSize() {
        assertEquals("1,5 KB", FileUtils.formatSize(1536));
        assertEquals("2,3 MB", FileUtils.formatSize(2411724));
        assertEquals("1,00 GB", FileUtils.formatSize(1073741824));
    }
}