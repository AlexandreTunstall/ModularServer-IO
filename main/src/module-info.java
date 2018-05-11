module atunstall.server.io {
    requires atunstall.server.core;
    exports atunstall.server.io.api;
    exports atunstall.server.io.api.fs;
    exports atunstall.server.io.api.util;
    exports atunstall.server.io.impl.fs to atunstall.server.core;
    exports atunstall.server.io.impl.util to atunstall.server.core;
}