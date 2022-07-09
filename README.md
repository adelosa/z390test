# z390 Test

This repo contains a framework for testing the z390 application.

See <https://github.com/z390development/z390> for the core project.

This is a black box test framework and does not perform unit testing
of the actual z390 code. It calls the z390 app via a shell command.

The idea is that you can run z390 with input and then check the output 
from the process.

## Setup

You will need a Java SDK installed - version 8 or above.

You will need the z390 source repository cloned to your local system.

    git clone https://github.com/z390development/z390.git
    cd z390

Build the z390 Java jar file for use by the tests. From the z390 root:

    bash/bldjar
    win> bat\BLDJAR.BAT

Some tests will require the linklib programs assembled

    bash/bldlib
    win> bat\BLDLIB.BAT

## Run the tests

Set the Z390_SOURCE_ROOT environment variable so the tests can find the Z390.jar and source.

    export Z390_SOURCE_ROOT /loc/of/z390
    win> SET Z390_SOURCE_ROOT=c:\loc\of\z390

By default the tests will not show the output of the `printOutput` method (see below).
While developing tests, it is useful to see this output.

    export Z390_PRINT_OUTPUT 1
    win> SET Z390_PRINT_OUTPUT=1

Now run the tests from this directory

    ./gradlew test
    win> gradlew test

    
## Writing test cases

The following is a basic test case that executes an assembly of a module and checks the return code.

```groovy
import org.junit.jupiter.api.Test

class TestZ390Test extends z390Test {

    var options = ['trace', 'noloadhigh', "SYSMAC(${basePath("mac")})"]

    @Test
    void test_file_source() {
        int rc = this.asm(basePath('tests', 'TESTINS1'), *options)
        this.printOutput()
        assert rc == 0
    }
}
```

To use the standard framework, your class should extend `z390Test`
which provides methods for running and capturing the output.

The tests use standard JUnit test structures.

## Methods for testing

The class embeds a number of methods for interacting with z390 similar
to the scripts you would use.

### assemble from file

```groovy
int rc = this.asm(basePath('tests', 'TESTINS1'), *options)
assert rc == 0
```
Parameters:
* asmFile - The HLASM file without prefix to assemble. Use basePath method to build path relative to z390.
* args... - 0-n parmeters to pass to the asm program, generally options.

Returns:
* rc - The result code from the execution

Methods available :
* asm - Assemble only
* asml - Assemble and link. Any non zero RC on assemble will exit with RC
* asmlg - Assemble, link and go. Any non zero RC on assemble and link will exit with RC
* mz390 - Assemble only
* cblclg - COBOL assemble, link and go. Any non-zero return will exist with RC
* lz390 - Link only. Output not cleared prior to run
* ez390 - Run only. Output not cleared prior to run

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
String sourceFilename = createTempFile("INLINE.MLC", source)
int rc = this.asm(sourceFilename, *options)
assert rc == 0
```
Use the `createTempFile` method to create a file that can be
used in the assembly.

### Set environment variables

```groovy
this.env = ['SNAPOUT': basePath('zopcheck', 'SNAPOUT.TXT')]
```
The env class property can be set with a hashlist of environment variable that will be passed
to the z390 calls.

To just add a single envvar without replacing the existing
```groovy
this.env.put('SNAPOUT', basePath('zopcheck', 'SNAPOUT.TXT'))
```

### loadFile -- loads content of a file into fileData

```groovy
loadFile(filename, label)
```
* filename - is the file that will be loaded into the fileData area.
* label - the key in the fileData hashlist

You can access the file contents after loading the file

```groovy
fileData[label] 
```

### printOutput -- prints captured data

```groovy
printOutput()
```
Prints the captured output to stdout - useful for debugging output.

Captured output includes stdout, stderr, the assembly output files (LST, LOG, PRN and ERR files) 
and any other files you load using the `loadFile` method.

First 5 digits are line numbers starting at 0 are included in the listing.

### basePath -- construct path relative to z390 source root

```groovy
basePath("tests", "TESTINS1")
```

Use this to construct file paths relative to the z390 source root. Ensures platform independent 
file handling.

Set the z390 source root using envvar `Z390_SOURCE_ROOT`. If not provided, defaults to "../z390"

### createTempFile -- create a temp file for use in the test

```groovy
var tempFileContents = """Line 1: Some text
Line 2: This is a test file
"""
var tempFilename = createTempFile("myTempFile.txt", tempFileContents)
println(tempFilename)
```

Use this to create temp files used in your tests. Returns full path to temp filename.

Note - Temp directory is deleted at end of testcase.
