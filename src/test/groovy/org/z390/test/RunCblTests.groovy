package org.z390.test

import org.junit.jupiter.api.Test

class RunCblTests extends z390Test{
    RunCblTests() {
        printOutput = true
    }

    @Test
    void test_COBOL_TESTADD1() {
        int rc = this.cblclg(basePath("zcobol", "tests", "TESTADD1"))
        this.printOutput()
        assert rc == 0
    }

    @Test
    void test_COBOL_TESTADD2() {
        int rc = this.cblclg(basePath("zcobol", "tests", "TESTADD2"))
        this.printOutput()
        assert rc == 0
    }

    @Test
    void test_COBOL_TESTIF1() {
        int rc = this.cblclg(basePath("zcobol", "tests", "TESTIF1"))
        this.printOutput()
        assert rc == 0
    }

    @Test
    void test_COBOL_TESTIF2() {
        int rc = this.cblclg(basePath("zcobol", "tests", "TESTIF2"))
        this.printOutput()
        assert rc == 0
    }

    @Test
    void test_COBOL_TESTIF3() {
        int rc = this.cblclg(basePath("zcobol", "tests", "TESTIF3"))
        this.printOutput()
        assert rc == 0
    }

    @Test
    void test_COBOL_TESTMOV1() {
        int rc = this.cblclg(basePath("zcobol", "tests", "TESTMOV1"))
        this.printOutput()
        assert rc == 0
    }

    @Test
    void test_COBOL_TESTMOV2() {
        int rc = this.cblclg(basePath("zcobol", "tests", "TESTMOV2"))
        this.printOutput()
        assert rc == 0
    }

    @Test
    void test_COBOL_TESTMOV3() {
        int rc = this.cblclg(basePath("zcobol", "tests", "TESTMOV3"))
        this.printOutput()
        assert rc == 0
    }

}
