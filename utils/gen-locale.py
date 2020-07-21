#!/bin/python

import sys
import json

text = ""
with open(sys.argv[1]) as fp:
    text = fp.read();
    
data = json.loads(text);
    

if sys.argv[2] == "list":
    
    for key in data:
        print(data[key])
        
else:
    
    
    lines = []
    with open(sys.argv[2]) as fd:
        lines = fd.readlines()
        
    
    i = 0
    for key in data:
        data[key] = lines[i].rstrip("\n")
        i = i + 1
        
    
    print(json.dumps(data, indent=4, separators=(",\n", " : ")))
    
    

    
    
