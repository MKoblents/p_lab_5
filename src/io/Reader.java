package io;

import java.io.IOException;

public interface Reader {
    String nextLine() throws IOException;
    boolean hasNextLine() throws IOException;
}
