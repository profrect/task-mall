import PageData from '@/model/pageData';

class ResultPageData<T> {
  records: Array<T>;

  pageData: PageData;

  constructor(records: Array<T>, pageData: PageData) {
    this.records = records;
    this.pageData = pageData;
  }
}

export default ResultPageData;
