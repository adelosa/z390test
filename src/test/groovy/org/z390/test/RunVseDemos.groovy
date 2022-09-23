package org.z390.test

import org.junit.jupiter.api.Test

class RunVseDemos extends z390Test {

    var options = ["SYSMAC(${basePath("vse", "mac")}+${basePath("mac")})"]

    @Test
    void test_DEMO_DEMOVSE1() {
        int rc = this.asmlg(basePath("vse", "demo", "DEMOVSE1"), *options)
        this.printOutput()
        assert rc == 0
    }

    @Test
    void test_DEMO_DEMOVSE2() {
        int rc = this.asmlg(basePath("vse", "demo", "DEMOVSE2"), *options)
        this.printOutput()
        assert rc == 0
    }

    @Test
    void test_DEMO_DEMOVSE3() {
        this.env = [
            'SYSUT1': basePath('vse', 'demo', 'DEMOVSE3.TF1'),
            'SYSUT2': basePath('vse', 'demo', 'DEMOVSE3.TF2')
        ]
        int rc = this.asmlg(basePath("vse", "demo", "DEMOVSE3"), *options)
        this.printOutput()
        assert rc == 0
        loadFile('sysut1', basePath('vse', 'demo', 'DEMOVSE3.TF1'))
        loadFile('sysut1', basePath('vse', 'demo', 'DEMOVSE3.TF2'))
        assert this.fileData['sysut1'] == this.fileData['sysut2']
    }

}
