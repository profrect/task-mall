class Page<T> {
  records;

  pageNumber = 1;

  pageSize = 10;

  totalRow = 0;

  totalPage = 0;

  constructor(
    records?: Array<T>,
    pageNumber?: number,
    pageSize?: number,
    totalRow?: number,
    totalPage?: number
  ) {
    this.pageNumber = pageNumber || 1;
    this.pageSize = pageSize || 10;
    if (totalRow) this.totalRow = totalRow;
    if (totalPage) this.totalPage = totalPage;
    if (records) this.records = records;
  }

  updatePageData(newPage: Page<T>): void {
    this.pageSize = newPage.pageSize;
    this.pageNumber = newPage.pageNumber;
    this.totalRow = newPage.totalRow;
    this.totalPage = newPage.totalPage;
    this.records = newPage.records;
  }
}

export default Page;
