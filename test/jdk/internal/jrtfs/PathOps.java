/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.InvalidPathException;
import java.net.URI;
import java.io.IOException;

/**
 * @test
 * @summary Tests jrt path operations
 */

public class PathOps {

    static final java.io.PrintStream out = System.out;
    static FileSystem fs;

    private String input;
    private Path path;
    private Exception exc;

    private PathOps(String first, String... more) {
        out.println();
        input = first;
        try {
            path = fs.getPath(first, more);
            out.format("%s -> %s", first, path);
        } catch (Exception x) {
            exc = x;
            out.format("%s -> %s", first, x);
        }
        out.println();
    }

    Path path() {
        return path;
    }

    void fail() {
        throw new RuntimeException("PathOps failed");
    }

    void checkPath() {
        if (path == null) {
            throw new InternalError("path is null");
        }
    }

    void check(Object result, String expected) {
        out.format("\tExpected: %s\n", expected);
        out.format("\tActual: %s\n",  result);
        if (result == null) {
            if (expected == null) return;
        } else {
            // compare string representations
            if (expected != null && result.toString().equals(expected.toString()))
                return;
        }
        fail();
    }

    void check(Object result, boolean expected) {
        check(result, Boolean.toString(expected));
    }

    PathOps root(String expected) {
        out.println("check root");
        checkPath();
        check(path.getRoot(), expected);
        return this;
    }

    PathOps parent(String expected) {
        out.println("check parent");
        checkPath();
        check(path.getParent(), expected);
        return this;
    }

    PathOps name(String expected) {
        out.println("check name");
        checkPath();
        check(path.getFileName(), expected);
        return this;
    }

    PathOps element(int index, String expected) {
        out.format("check element %d\n", index);
        checkPath();
        check(path.getName(index), expected);
        return this;
    }

    PathOps subpath(int startIndex, int endIndex, String expected) {
        out.format("test subpath(%d,%d)\n", startIndex, endIndex);
        checkPath();
        check(path.subpath(startIndex, endIndex), expected);
        return this;
    }

    PathOps starts(String prefix) {
        out.format("test startsWith with %s\n", prefix);
        checkPath();
        Path s = fs.getPath(prefix);
        check(path.startsWith(s), true);
        return this;
    }

    PathOps notStarts(String prefix) {
        out.format("test not startsWith with %s\n", prefix);
        checkPath();
        Path s = fs.getPath(prefix);
        check(path.startsWith(s), false);
        return this;
    }

    PathOps ends(String suffix) {
        out.format("test endsWith %s\n", suffix);
        checkPath();
        Path s = fs.getPath(suffix);
        check(path.endsWith(s), true);
        return this;
    }

    PathOps notEnds(String suffix) {
        out.format("test not endsWith %s\n", suffix);
        checkPath();
        Path s = fs.getPath(suffix);
        check(path.endsWith(s), false);
        return this;
    }

    PathOps absolute() {
        out.println("check path is absolute");
        checkPath();
        check(path.isAbsolute(), true);
        return this;
    }

    PathOps notAbsolute() {
        out.println("check path is not absolute");
        checkPath();
        check(path.isAbsolute(), false);
        return this;
    }

    PathOps resolve(String other, String expected) {
        out.format("test resolve %s\n", other);
        checkPath();
        check(path.resolve(other), expected);
        return this;
    }

    PathOps resolveSibling(String other, String expected) {
        out.format("test resolveSibling %s\n", other);
        checkPath();
        check(path.resolveSibling(other), expected);
        return this;
    }

    PathOps relativize(String other, String expected) {
        out.format("test relativize %s\n", other);
        checkPath();
        Path that = fs.getPath(other);
        check(path.relativize(that), expected);
        return this;
    }

    PathOps normalize(String expected) {
        out.println("check normalized path");
        checkPath();
        check(path.normalize(), expected);
        return this;
    }

    PathOps string(String expected) {
        out.println("check string representation");
        checkPath();
        check(path, expected);
        return this;
    }

    PathOps isSameFile(String target) {
        try {
            out.println("check two paths are same");
            checkPath();
            check(Files.isSameFile(path, test(target).path()), true);
        } catch (IOException ioe) {
            fail();
        }
        return this;
    }

    PathOps invalid() {
        if (!(exc instanceof InvalidPathException)) {
            out.println("InvalidPathException not thrown as expected");
            fail();
        }
        return this;
    }

    static PathOps test(String s) {
        return new PathOps(s);
    }

    static PathOps test(String first, String... more) {
        return new PathOps(first, more);
    }

    // -- PathOpss --

    static void header(String s) {
        out.println();
        out.println();
        out.println("-- " + s + " --");
    }

    static void doPathOpTests() {
        header("Path operations");

        // construction
        test("/")
            .string("/");
        test("/", "")
            .string("/");
        test("/", "foo")
            .string("/foo");
        test("/", "/foo")
            .string("/foo");
        test("/", "foo/")
            .string("/foo");
        test("foo", "bar", "gus")
            .string("foo/bar/gus");
        test("")
            .string("");
        test("", "/")
            .string("/");
        test("", "foo", "", "bar", "", "/gus")
            .string("foo/bar/gus");

        // all components
        test("/a/b/c")
            .root("/")
            .parent("/a/b")
            .name("c");

        // root component only
        test("/")
            .root("/")
            .parent(null)
            .name(null);

        // no root component
        test("a/b")
            .root(null)
            .parent("a")
            .name("b");

        // name component only
        test("foo")
             .root(null)
             .parent(null)
             .name("foo");
        test("")
             .root(null)
             .parent(null)
             .name("");

        // startsWith
        test("")
            .starts("")
            .notStarts("/");
        test("/")
            .starts("/")
            .notStarts("")
            .notStarts("/foo");
        test("/foo")
            .starts("/")
            .starts("/foo")
            .notStarts("/f")
            .notStarts("");
        test("/foo/bar")
            .starts("/")
            .starts("/foo")
            .starts("/foo/")
            .starts("/foo/bar")
            .notStarts("/f")
            .notStarts("foo")
            .notStarts("foo/bar")
            .notStarts("");
        test("foo")
            .starts("foo")
            .notStarts("")
            .notStarts("f");
        test("foo/bar")
            .starts("foo")
            .starts("foo/")
            .starts("foo/bar")
            .notStarts("f")
            .notStarts("/foo")
            .notStarts("/foo/bar");

        // endsWith
        test("")
            .ends("")
            .notEnds("/");
        test("/")
            .ends("/")
            .notEnds("")
            .notEnds("foo")
            .notEnds("/foo");
        test("/foo")
            .ends("foo")
            .ends("/foo")
            .notEnds("/")
            .notEnds("fool");
        test("/foo/bar")
            .ends("bar")
            .ends("foo/bar")
            .ends("foo/bar/")
            .ends("/foo/bar")
            .notEnds("/bar");
        test("/foo/bar/")
            .ends("bar")
            .ends("foo/bar")
            .ends("foo/bar/")
            .ends("/foo/bar")
            .notEnds("/bar");
        test("foo")
            .ends("foo")
            .notEnds("")
            .notEnds("oo")
            .notEnds("oola");
        test("foo/bar")
            .ends("bar")
            .ends("bar/")
            .ends("foo/bar/")
            .ends("foo/bar")
            .notEnds("r")
            .notEnds("barmaid")
            .notEnds("/bar")
            .notEnds("ar")
            .notEnds("barack")
            .notEnds("/bar")
            .notEnds("o/bar");
        test("foo/bar/gus")
            .ends("gus")
            .ends("bar/gus")
            .ends("foo/bar/gus")
            .notEnds("g")
            .notEnds("/gus")
            .notEnds("r/gus")
            .notEnds("barack/gus")
            .notEnds("bar/gust");

        // elements
        test("a/b/c")
            .element(0,"a")
            .element(1,"b")
            .element(2,"c");

        // isAbsolute
        test("/")
            .absolute();
        test("/tmp")
            .absolute();
        test("tmp")
            .notAbsolute();
        test("")
            .notAbsolute();

        // resolve
        test("/tmp")
            .resolve("foo", "/tmp/foo")
            .resolve("/foo", "/foo")
            .resolve("", "/tmp");
        test("tmp")
            .resolve("foo", "tmp/foo")
            .resolve("/foo", "/foo")
            .resolve("", "tmp");
        test("")
            .resolve("", "")
            .resolve("foo", "foo")
            .resolve("/foo", "/foo");

        // resolveSibling
        test("foo")
            .resolveSibling("bar", "bar")
            .resolveSibling("/bar", "/bar")
            .resolveSibling("", "");
        test("foo/bar")
            .resolveSibling("gus", "foo/gus")
            .resolveSibling("/gus", "/gus")
            .resolveSibling("", "foo");
        test("/foo")
            .resolveSibling("gus", "/gus")
            .resolveSibling("/gus", "/gus")
            .resolveSibling("", "/");
        test("/foo/bar")
            .resolveSibling("gus", "/foo/gus")
            .resolveSibling("/gus", "/gus")
            .resolveSibling("", "/foo");
        test("")
            .resolveSibling("foo", "foo")
            .resolveSibling("/foo", "/foo")
            .resolve("", "");

        // relativize
        test("/a/b/c")
            .relativize("/a/b/c", "")
            .relativize("/a/b/c/d/e", "d/e")
            .relativize("/a/x", "../../x")
            .relativize("/x", "../../../x");
        test("a/b/c")
            .relativize("a/b/c/d", "d")
            .relativize("a/x", "../../x")
            .relativize("x", "../../../x")
            .relativize("", "../../..");
        test("")
            .relativize("a", "a")
            .relativize("a/b/c", "a/b/c")
            .relativize("", "");

        // normalize
        test("/")
            .normalize("/");
        test("foo")
            .normalize("foo");
        test("/foo")
            .normalize("/foo");
        test(".")
            .normalize("");
        test("..")
            .normalize("..");
        test("/..")
            .normalize("/");
        test("/../..")
            .normalize("/");
        test("foo/.")
            .normalize("foo");
        test("./foo")
            .normalize("foo");
        test("foo/..")
            .normalize("");
        test("../foo")
            .normalize("../foo");
        test("../../foo")
            .normalize("../../foo");
        test("foo/bar/..")
            .normalize("foo");
        test("foo/bar/gus/../..")
            .normalize("foo");
        test("/foo/bar/gus/../..")
            .normalize("/foo");
        test("/./.")
            .normalize("/");
        test("/.")
            .normalize("/");
        test("/./abc")
            .normalize("/abc");
        // invalid
        test("foo\u0000bar")
            .invalid();
        test("\u0000foo")
            .invalid();
        test("bar\u0000")
            .invalid();
        test("//foo\u0000bar")
            .invalid();
        test("//\u0000foo")
            .invalid();
        test("//bar\u0000")
            .invalid();

        // normalization
        test("//foo//bar")
            .string("/foo/bar")
            .root("/")
            .parent("/foo")
            .name("bar");

        // isSameFile
        test("/fileDoesNotExist")
            .isSameFile("/fileDoesNotExist");
    }

    static void npes() {
        header("NullPointerException");

        Path path = fs.getPath("foo");

        try {
            path.resolve((String)null);
            throw new RuntimeException("NullPointerException not thrown");
        } catch (NullPointerException npe) {
        }

        try {
            path.relativize(null);
            throw new RuntimeException("NullPointerException not thrown");
        } catch (NullPointerException npe) {
        }

        try {
            path.compareTo(null);
            throw new RuntimeException("NullPointerException not thrown");
        } catch (NullPointerException npe) {
        }

        try {
            path.startsWith((Path)null);
            throw new RuntimeException("NullPointerException not thrown");
        } catch (NullPointerException npe) {
        }

        try {
            path.endsWith((Path)null);
            throw new RuntimeException("NullPointerException not thrown");
        } catch (NullPointerException npe) {
        }

    }

    public static void main(String[] args) throws Throwable {
        fs = FileSystems.getFileSystem(URI.create("jrt:/"));
        npes();
        doPathOpTests();
    }
}
