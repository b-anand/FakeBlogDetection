LowBow Character based N-gram author attribution:
================================================

Paper: 
	http://aclweb.org/anthology-new/P/P11/P11-1030.pdf
Code: 
	KernelSmoothingSample [Folder]
Dataset:
	1. http://www.cs.utexas.edu/users/sindhu/acl2010
	CompressedDataset [Folder]: Each of below contains a parameters.txt file which contains the details of parameters set while obtaining this dataset.
		ProcessedDataset_2_0.2.zip
		ProcessedDataset_2_0.2_csv.zip
		ProcessedDataset_5_0.2.zip
		Dataset.zip
		Datasubset.zip
	2. http://www.cs.cmu.edu/afs/cs.cmu.edu/project/theo-20/www/data/


TFIDF with SVM:
==============

Code:
	ExtractDataFromBlogs [Folder]: Code used to get the valid list of RSS feeds for the given blog links (html data)
	ExtractBlogData [Folder]: Extract data from RSS feeds and save them to xml files.
	store_data [Folder]: Read the xml files containing Bad Blog Data and store them into SQL database.
	StoreDataIntoDB [Folder]: Store the xml files for Good Blog Data into SQL database.
	TFIDF_to_SVD [Folder]: Code used to read the TFIDF vectors from the Bad blog data obtained using Rapidminer and do SVD on it.	 

Dataset:
	CompressedDataset [Folder]:
		ProcessedData2012_04_23.sql.7z: Contains the SQL Dump for blog data that was stored in mysql database.
		ProcessedDataBlogPosts20120425.sql.7z: Contains the SQL Dump for blog data (each blog post is a seperate row) that was stored in mysql database.
		blogDataSet.tar.gz: XML files for the RSS feeds corresponding to bad blogs.
		goodBlogDataSet.7z: XML files for the RSS feeds corresponding to good blogs.
		Rapidminer_Repository: Rapidminer process and dataset used for building the model.
		good_blogs_feed_list.txt, good_blogs_list.tsv: Good blog rss feed url list.
		SampleBlacklist.xlsx: Bad blog rss feed url list.