#!/usr/bin/env python

# ===-- ktest-tool --------------------------------------------------------===##
# 
#                      The KLEE Symbolic Virtual Machine
# 
#  This file is distributed under the University of Illinois Open Source
#  License. See LICENSE.TXT for details.
# 
# ===----------------------------------------------------------------------===##
#
#
#  Modified to parse all files in a given folder anad to get the used values from the data.
#  These values are then stored in a file called out.txt
#
# ===----------------------------------------------------------------------===##

import os
import struct
import sys

version_no=3

class KTestError(Exception):
    pass

class KTest:
    @staticmethod
    def fromfile(path):
        if not os.path.exists(path):
            print("ERROR: file %s not found" % (path))
            sys.exit(1)
            
        f = open(path,'rb')
        hdr = f.read(5)
        if len(hdr)!=5 or (hdr!=b'KTEST' and hdr != b"BOUT\n"):
            raise KTestError('unrecognized file')
        version, = struct.unpack('>i', f.read(4))
        if version > version_no:
            raise KTestError('unrecognized version')
        numArgs, = struct.unpack('>i', f.read(4))
        args = []
        for i in range(numArgs):
            size, = struct.unpack('>i', f.read(4))
            args.append(str(f.read(size).decode(encoding='ascii')))
            
        if version >= 2:
            symArgvs, = struct.unpack('>i', f.read(4))
            symArgvLen, = struct.unpack('>i', f.read(4))
        else:
            symArgvs = 0
            symArgvLen = 0

        numObjects, = struct.unpack('>i', f.read(4))
        objects = []
        for i in range(numObjects):
            size, = struct.unpack('>i', f.read(4))
            name = f.read(size)
            size, = struct.unpack('>i', f.read(4))
            bytes = f.read(size)
            objects.append( (name,bytes) )

        # Create an instance
        b = KTest(version, args, symArgvs, symArgvLen, objects)
        # Augment with extra filename field
        b.filename = path
        return b
    
    def __init__(self, version, args, symArgvs, symArgvLen, objects):
        self.version = version
        self.symArgvs = symArgvs
        self.symArgvLen = symArgvLen
        self.args = args
        self.objects = objects

        # add a field that represents the name of the program used to
        # generate this .ktest file:
        program_full_path = self.args[0]
        program_name = os.path.basename(program_full_path)
        # sometimes program names end in .bc, so strip them
        if program_name.endswith('.bc'):
          program_name = program_name[:-3]
        self.programName = program_name
        
def trimZeros(str):
    for i in range(len(str))[::-1]:
        if str[i] != '\x00':
            return str[:i+1]
    return ''
    
def main(args):
    from optparse import OptionParser
    op = OptionParser("usage: %prog [options] files")
    op.add_option('','--trim-zeros', dest='trimZeros', action='store_true', 
                  default=False,
                  help='trim trailing zeros')
    op.add_option('','--write-ints', dest='writeInts', action='store_true',
                  default=False,
                  help='convert 4-byte sequences to integers')
                  
    op.add_option('','--verbose', dest='verbose', action='store_true',
                  default=False,
                  help='Show verbose output')
    
    opts,args = op.parse_args()
    if not args and not len(args) is 1:
        op.error("incorrect number of arguments")

    arg=args[0]
    if not os.path.isdir(arg):
        op.error("not a folder")

    with open('out.txt', 'w') as resfile:
        for file in filter(lambda f: f.endswith('.ktest'), os.listdir(arg)):
            file = arg + os.sep + file        
            b = KTest.fromfile(file)
            pos = 0
            if opts.verbose:
				print('ktest file : %r' % file)
            for i,(name,data) in enumerate(b.objects):
                if opts.trimZeros:
                    out = trimZeros(data)
                else:
                    out = data

                if opts.writeInts and len(data) == 4: 
                    print('error on data:')
                    print('%r' % struct.unpack('i',out)[0])             
                    exit()
                else:
                    vals = map(lambda x: str(ord(x[1])), filter(lambda x: x[0] % 4 == 0,enumerate(out)))
                    resfile.write(', '.join(vals) + '\n')
                    if opts.verbose:
						print('%s' % vals) 


if __name__=='__main__':
    main(sys.argv)
