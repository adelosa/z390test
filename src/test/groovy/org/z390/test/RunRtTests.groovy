package org.z390.test

import org.junit.jupiter.api.Test

class RunRtTests extends z390Test {
    var sysmac = pathJoin(this.project_root, "mac")
    var syscpy = pathJoin(this.project_root, "mac")
    var libs = ["SYSMAC(${sysmac}+)", "SYSCPY(${syscpy}+)"]
    // rt1 = asmlg
    var rt1Options = ['bal', 'notiming', 'stats', *libs]
    // rt3 = mz390
    var rt3Options = ['noasm', 'bal', 'notiming', 'stats', *libs]
    // rt4 = mz390
    var rt4Options = ['noasm',  'bal', 'notiming', 'stats', *libs]
    // rt7 = asml + ez390
    var rt7AsmlOptions = ['bal', 'notiming', *libs]
    var rt7Ez390Options = ['notiming', 'stats', *libs]

    @Test
    void test_TESTERR1() {
        // rt3 = mz390
        var syscpyRtTest = "SYSCPY(${pathJoin(this.project_root, 'rt', 'test')})"
        int rc = this.mz390(pathJoin("rt", "test", "TESTERR1"), *rt3Options, syscpyRtTest)
        this.printOutput()
        assert rc == 16
    }
    @Test
    void test_TESTERR2() {
        // rt4 = mz390
        int rc = this.mz390(pathJoin("rt", "test", "TESTERR2"), *rt4Options, 'ERR(0)')
        this.printOutput()
        assert rc == 16
    }
    @Test
    void test_TESTERR3() {
        // rt3 = mz390
        int rc = this.mz390(pathJoin("rt", "test", "TESTERR3"), *rt3Options)
        this.printOutput()
        assert rc == 12
    }
    @Test
    void test_TESTERR4() {
        // rt7 = asml + mz390
        int rc
        rc = this.asml(pathJoin("rt", "test", "TESTERR4"), *rt7AsmlOptions)
        assert rc == 0
        rc = this.ez390(pathJoin("rt", "test", "TESTERR4"), *rt7Ez390Options)
        this.printOutput()
        assert rc == 0
    }
    @Test
    void test_TESTERR5() {
        // rt1 = asmlg
        int rc = this.asmlg(pathJoin("rt", "test", "TESTERR5"), *rt1Options)
        this.printOutput()
        assert rc == 0
    }
    @Test
    void test_TESTERR6() {
        // rt7 = asml + mz390
        int rc
        rc = this.asml(pathJoin("rt", "test", "TESTERR6"), *rt7AsmlOptions)
        assert rc == 0
        rc = this.ez390(pathJoin("rt", "test", "TESTERR6"), *rt7Ez390Options)
        this.printOutput()
        assert rc == 16
    }
    @Test
    void test_TESTERR7() {
        // rt7 = asml + mz390
        this.env = ['TESTERR7': 'afile.txt']
        int rc
        rc = this.asml(pathJoin("rt", "test", "TESTERR7"), *rt7AsmlOptions)
        assert rc == 0
        rc = this.ez390(pathJoin("rt", "test", "TESTERR7"), *rt7Ez390Options)
        this.printOutput()
        assert rc == 16
    }
    @Test
    void test_TESTERR8() {
        // rt3 = mz390
        int rc = this.mz390(pathJoin("rt", "test", "TESTERR8"), *rt3Options)
        this.printOutput()
        assert rc == 12
    }
    @Test
    void test_TESTERR9() {
        // rt3 = mz390
        int rc = this.mz390(pathJoin("rt", "test", "TESTERR9"), *rt3Options)
        this.printOutput()
        assert rc == 16
    }
    @Test
    void test_TESTERRA() {
        // rt4 = mz390
        int rc = this.mz390(pathJoin("rt", "test", "TESTERRA"), 'CHKMAC(2)', *rt4Options)
        this.printOutput()
        assert rc == 16
    }
}