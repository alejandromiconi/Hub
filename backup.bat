set mydate=
del *.zip

@for /f "skip=1" %%x in ('wmic os get localdatetime') do if not defined mydate set mydate=%%x
echo Current date is %mydate%

set ZIP="D:\Program Files\7-Zip\7z"


set FILE=hub%mydate%.zip
%ZIP% a %FILE% . -xr!*.zip

set DEST="D:\Onedrive\Backups\Dev"

MOVE *.zip %DEST%

pause