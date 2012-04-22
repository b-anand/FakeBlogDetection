set start=10000
set end=15000
set step=1000
FOR /L %%i IN (%start%,%step%,%end%) DO (
	start python blog_processor.py %%i %step%
)

