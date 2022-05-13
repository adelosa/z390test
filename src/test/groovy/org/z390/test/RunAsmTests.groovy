package org.z390.test

import org.junit.jupiter.api.Test

class RunAsmTests extends z390Test {

    var sysmac = pathJoin(this.project_root, "mac")
    var options = ['trace', 'noloadhigh', "SYSMAC(${sysmac})"]

    @Test
    void test_TESTINS1() {
        int rc = this.asm(pathJoin("tests", "TESTINS1"), *options)
        this.printOutput()
        assert rc == 0
    }
    @Test
    void test_TESTINS2() {
        int rc = this.asmlg(pathJoin("tests", "TESTINS2"), *options)
        this.printOutput()
        assert rc == 0
    }
    @Test
    void test_TESTINS3() {
        int rc = this.asmlg(pathJoin("tests", "TESTINS3"), *options)
        this.printOutput()
        assert rc == 0
    }
    @Test
    void test_TESTINS4() {
        int rc = this.asmlg(pathJoin("tests", "TESTINS4"), *options)
        this.printOutput()
        assert rc == 0
    }
    @Test
    void test_TESTINS5() {
        int rc = this.asmlg(pathJoin("tests", "TESTINS5"), *options, 'optable(z390)')
        this.printOutput()
        assert rc == 0
    }
    @Test
    void test_TESTDFP1() {
        int rc = this.asmlg(pathJoin("tests", "TESTDFP1"), *options, 'optable(z390)')
        this.printOutput()
        assert rc == 0
    }
    @Test
    void test_TESTDFP2() {
        int rc = this.asmlg(pathJoin("tests", "TESTDFP2"), *options, 'optable(z390)')
        this.printOutput()
        assert rc == 0
    }
    @Test
    void test_TESTLITS() {
        int rc = this.asmlg(pathJoin("tests", "TESTLITS"), *options, 'trace', 'noloadhigh')
        this.printOutput()
        assert rc == 0
    }
    @Test
    void test_TESTAMPS() {
        int rc = this.asmlg(pathJoin("rt", "mlc", "TESTAMPS"), *options, 'optable(z390)')
        this.printOutput()
        assert rc == 0
    }
    @Test
    void test_IS215() {
        int rc = this.asmlg(pathJoin("rt", "mlc", "IS215"), *options, 'optable(z390)')
        this.printOutput()
        assert rc == 0
    }
    @Test
    void test_ZOPCHECK() {
        var syscpy1 = pathJoin(this.project_root, 'zopcheck')
        var syscpy2 = pathJoin(this.project_root, 'mac')
        var syscpyOption = "SYSCPY(${syscpy1}+${syscpy2})"
        this.env = ['SNAPOUT': pathJoin(this.project_root, 'zopcheck', 'SNAPOUT.TXT')]
        int rc = this.asmlg(pathJoin("zopcheck", "ZOPCHECK"), *options, syscpyOption, 'optable(z390)')
        this.printOutput()
        assert rc == 0
    }

}


