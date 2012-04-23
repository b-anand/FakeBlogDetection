set start=10000
set end=22000
set step=1000
FOR /L %%i IN (%start%,%step%,%end%) DO (
	start java -cp bin ExtractData %%i %step%
)

