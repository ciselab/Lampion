# Subword NMT Test

This repository helps me to figure the pre-processing for the [OpenVocabCodeNLM](https://github.com/mast-group/OpenVocabCodeNLM) out.

This is focussed on `.py`-files and uses the [subword-nmt-library](https://github.com/rsennrich/subword-nmt).

The repository encodings were drawn from [Zenodo](https://zenodo.org/record/3628636).
I do intend to keep the original encodings after augmentation.

Raw data can be found [here](https://zenodo.org/record/3628784) and [models here](https://zenodo.org/record/3628628).

## Requirements

```console
pip install subword-nmt==0.3.8
```

## How To

General usage:
```
subword-nmt apply-bpe -c {codes_file} < {test_file} > {out_file}
```

Sample usage:
```
subword-nmt apply-bpe -c ./python_encodings/python_encoding.enc_bpe_10000 < ./my_sample_files/A.py > output.txt
```

This is applied per file (?) and yields a new file with the code sliced per byte-encoding:

``` 
if __name__ == '__main@@ __@@ '@@ :
    s@@ ome = "@@ Le@@ on@@ har@@ d"
    print@@ (@@ "@@ Th@@ e main@@ tain@@ er her@@ e is ",@@ s@@ ome@@ )
```
Where `@@` marks, that the token is not "closed", i.e. there is a connection to the one after.


## Expected output:

From the sample data (python small) in the OpenVocabCodeNLM: 

``` 
def squ@@ are_@@ root ( x , gu@@ ess = 1 ) : def imp@@ ro@@ ve_@@ answer ( ) : def aver@@ age ( a , b ) : return ( a + b ) / 2.0 return aver@@ age ( gu@@ ess , x / gu@@ ess ) def goo@@ d_@@ en@@ ou@@ g@@ h ( ) : goo@@ d = abs ( gu@@ ess * gu@@ ess - x ) return goo@@ d < 0.00@@ 1 while not goo@@ d_@@ en@@ ou@@ g@@ h ( ) : gu@@ ess = imp@@ ro@@ ve_@@ answer ( ) return gu@@ ess if __name__ == '__main__' : x = float ( raw_input ( "" ) ) sqrt = squ@@ are_@@ root ( x ) print "@@ sq@@ rt@@ (@@ %@@ f@@ ) = %@@ f" % ( x , sqrt ) print "@@ sq@@ r@@ d@@ (@@ %@@ f@@ ) = %@@ f" % ( sqrt , sqrt * sqrt )
#!/usr/bin/env python class Test : def __init__ ( self , a , b ) : self . a = a ; self . b = b ; def main ( ) : x = Test ( 10 , 3 ) print x . a print x . b if __name__ == '__main__' : main ( )
#!/usr/bin/python import sys import urllib url = sys . argv [ 1 ] path = sys . argv [ 2 ] print url file = urllib . urlopen ( url ) print '' info = file . info ( ) print info urllib . url@@ retri@@ ev@@ e ( url , path ) if info . get@@ type ( ) == 'text/@@ html' : text = file . read ( )
#!/usr/bin/python import sys def main ( ) : n = 10 print n def add ( ) : n + 10 print n if __name__ == '__main__' : main ( )
from __future__ import unicode_literals import logging from sm@@ tp@@ lib import SM@@ T@@ P@@ Exception from django import forms from django.@@ core import mail from django.@@ template@@ .@@ loader import render_to_@@ string from django.utils.@@ translation import u@@ gettext_@@ lazy as _ from en@@ ve@@ lop@@ e import settings from en@@ ve@@ lop@@ e.@@ signals import after_@@ send logger = logging . getLogger ( 'en@@ ve@@ lop@@ e.@@ form@@ s' ) class Cont@@ act@@ Form ( forms . Form ) : sender = forms . CharField ( label = _ ( "F@@ ro@@ m" ) ) email = forms . Email@@ Field ( label = _ ( "E@@ mail@@ " ) ) subject = forms . CharField ( label = _ ( "S@@ ub@@ jec@@ t" ) , required = False ) message = forms . CharField ( label = _ ( "@@ Message@@ " ) , widget = forms . T@@ ex@@ ta@@ re@@ a ( ) ) sub@@ ject_@@ in@@ tr@@ o = settings . SUB@@ JECT_@@ IN@@ TR@@ O from_@@ email = settings . F@@ RO@@ M_@@ EMAI@@ L email_@@ recipi@@ ents = settings . EMAI@@ L_@@ REC@@ IP@@ I@@ ENT@@ S template_name = '' html_@@ template_name = '' def __init__ ( self , * args , * * kwargs ) : for kw@@ arg in list ( kwargs ) : if hasattr ( self , kw@@ arg ) : setattr ( self , kw@@ arg , kwargs . pop ( kw@@ arg ) ) super ( Cont@@ act@@ Form , self ) . __init__ ( * args , * * kwargs ) def save ( self ) : subject = self . get_@@ subject ( ) from_@@ email = self . get_@@ from_@@ email ( ) email_@@ recipi@@ ents = self . get_@@ email_@@ recipi@@ ents ( ) context = self . get_@@ context ( ) message_@@ body = render_to_@@ string ( self . get_@@ template_@@ names ( ) , context ) try : message = mail . Email@@ Multi@@ Al@@ tern@@ ati@@ ves ( subject = subject , body = message_@@ body , from_@@ email = from_@@ email , to = email_@@ recipi@@ ents , headers = { 'Re@@ pl@@ y@@ -@@ To@@ ' : self . cleaned_data [ 'email' ] } ) if settings . USE_@@ HTM@@ L_@@ EMAI@@ L : html_@@ body = render_to_@@ string ( self . html_@@ template_name , context ) message . attach@@ _@@ al@@ tern@@ ative ( html_@@ body , "@@ text/@@ html" ) message . send ( ) after_@@ send . send ( sender = self . __class__ , message = message , form = self ) logger . info ( _ ( "" ) % self . cleaned_data [ 'email' ] ) except SM@@ T@@ P@@ Exception : logger . exception ( _ ( "" ) ) return False else : return True def get_@@ context ( self ) : return self . cleaned_data . copy ( ) def get_@@ subject ( self ) : return self . sub@@ ject_@@ in@@ tr@@ o + self . cleaned_data [ 'sub@@ ject' ] def get_@@ from_@@ email ( self ) : return self . from_@@ email def get_@@ email_@@ recipi@@ ents ( self ) : return self . email_@@ recipi@@ ents def get_@@ template_@@ names ( self ) : return self . template_name
import os BASE_@@ DIR = os . path . abspath ( os . path . dirname ( __file__ ) ) TEMPLATE_@@ DEBUG = DEBUG = False AL@@ LOW@@ ED_@@ HO@@ ST@@ S = [ '*' ] MAN@@ AG@@ ERS = ADM@@ IN@@ S = ( ) DAT@@ AB@@ AS@@ ES = { 'default' : { '@@ EN@@ G@@ IN@@ E' : '' , '@@ NAME' : os . path . join ( BASE_@@ DIR , '@@ example.@@ db' ) , '@@ USER@@ ' : '' , 'P@@ ASS@@ WOR@@ D' : '' , '@@ HO@@ ST' : '' , 'P@@ OR@@ T' : '' , } } EMAI@@ L_@@ BACK@@ END = '' DEFAULT_@@ F@@ RO@@ M_@@ EMAI@@ L = '' TIME_@@ Z@@ ON@@ E = 'A@@ mer@@ ic@@ a/@@ Ch@@ ic@@ a@@ go@@ ' LAN@@ GU@@ AGE_@@ CODE = 'en@@ -@@ us' USE_@@ I@@ 18@@ N = True USE_@@ L@@ 10@@ N = True SE@@ CRE@@ T_@@ KEY = '' TEMPLATE_@@ LO@@ AD@@ ERS = ( '' , '' , ) M@@ ID@@ D@@ LE@@ WARE_@@ CLAS@@ SE@@ S = ( '' , '' , '' , '' , '' , '' , ) ROO@@ T_@@ URL@@ CONF = '' TEMPLATE_@@ DIR@@ S = ( os . path . join ( BASE_@@ DIR , '@@ templat@@ es' ) , ) INST@@ AL@@ LED_@@ APP@@ S = ( '' , '' , '' , '' , 'en@@ ve@@ lop@@ e' , 'h@@ one@@ yp@@ o@@ t' , 'c@@ ri@@ sp@@ y_@@ form@@ s' , ) HO@@ NE@@ Y@@ PO@@ T_@@ FIEL@@ D_@@ NAME = 'email@@ 2'
[ ... Tons and Tons More ... ]
```

These are full files written, one per line. 
Whitespace is completely removed, when looking at the example.

Mine has a bit more `@@`´s, but that is likely because I didn't lint it and now the brackets are connected.