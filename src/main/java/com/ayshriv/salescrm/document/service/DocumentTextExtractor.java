package com.ayshriv.salescrm.document.service;

import java.io.IOException;
import java.io.InputStream;

public interface DocumentTextExtractor {
    String extractText(InputStream inputStream, String filename, String contentType) throws IOException;
    String extractText(byte[] bytes, String filename, String contentType) throws IOException;
}
