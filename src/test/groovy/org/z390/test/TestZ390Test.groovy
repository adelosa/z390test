package org.z390.test

import org.junit.jupiter.api.Test

class TestZ390Test extends z390Test {

    var sysmac = pathJoin(this.project_root, "mac")
    var options = ['trace', 'noloadhigh', "SYSMAC(${sysmac})"]

    @Test
    void testAsm() {
        int rc = this.asm(pathJoin("tests", "TESTINS1"), *options)
        this.printOutput()
        assert rc == 0
    }

    @Test
    void testAsml() {
        int rc = this.asml(pathJoin("tests", "TESTINS2"), *options)
        this.printOutput()
        assert rc == 0
    }

    @Test
    void testAsmlg() {
        int rc = this.asmlg(pathJoin("tests", "TESTINS2"), *options)
        this.printOutput()
        assert rc == 0
    }

    @Test
    void testInlineSource() {

        var source = """TESTB    START 0
         USING *,13
         STM   14,12,12(13)
         ST    13,8(13)
         ST    15,4(15)
         LR    13,15
         J
         RETURN (14,12)
         END   TESTB
    """
        int rc = this.asm("INLINE", asm_source: source, *options)
        this.printOutput()
        assert rc == 12   // Check return code
        assert this.fileOutput['ERR'] =~ /AZ390 AZ390I invalid relatvie offset expression/  // check error present
    }
}


