# z390 Test

This repo contains a framework for testing the z390 application.

See <https://github.com/z390development/z390> for the core project.

This is a black box test framework and does not perform unit testing
of the actual z390 code. It calls the z390 app via a shell command.

The idea is that you can run z390 with input and then check the output 
from the process.

## Writing test cases

The following is a basic test case that executes an assembly of a
module and checks the return code.

```groovy
import org.junit.jupiter.api.Test

class TestZ390Test extends z390Test {

    var sysmac = pathJoin(this.project_root, "mac")
    var options = ['trace', 'noloadhigh', "SYSMAC(${sysmac})"]

    @Test
    void test_file_source() {
        int rc = this.asm(pathJoin("tests", "TESTINS1"), *options)
        this.printOutput()
        assert rc == 0
    }
}
```

To use the standard framework, your class should extend `org.z390.test.z390Test`
which provided methods for running and capturing the output.

The tests are using standard JUnit test structures

## Methods for testing

The class embeds a number of methods for interacting with z390 similar
to the scripts you would use.

### assemble from file

```groovy
int rc = this.asm(pathJoin("tests", "TESTINS1"), *options)
assert rc == 0
```
Parameters:
* asmFile - The HLASM file to assemble. Path relative to z390 project
* args... - 0-n parmeters to pass to the asm program, generally options.

Returns:
* rc - The result code from the execution

### assemble from inline source

```groovy
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
assert rc == 0
```
Parameters:
* asmFile - The HLASM filename to assemble. Do not include path, just name.
* args... - 0-n parmeters to pass to the asm program, generally options.
* asm_source -  Provides source instead of from file.

Returns:
* rc - The result code from the execution


### printOutput -- prints captured output

```groovy
printOutput()
```
Prints the captured output to stdout - useful for debugging output.
Captured output includes stdout, stderr and the LST, LOG, PRN and ERR files
generated by the process.

First 5 digits are line numbers starting at 0.

### pathJoin -- construct path from files

```groovy
pathJoin("tests", "TESTINS1")
```

Use this to contruct file paths. Ensures platform independent 
file handling.