# Search Functionality Specifications

| Search Term         |
|---------------------|
| Headphones          |
| Smart Watch         |
| Mechanical Keyboard |

## 1. Valid Product Search and Title Verification
tags: smoke, search
* Navigate to home page and accept cookies
* Search for product "Laptop"
* Verify search results are displayed
* Verify first product title is not empty

## 2. Data-Driven Product Search with Keyword Table
tags: regression, search, data-driven
* Navigate to home page and accept cookies
* Search for product <Search Term>
* Verify search results are displayed for <Search Term>

## 3. Invalid Keyword Search Displays No Results Message
tags: negative, search
* Navigate to home page and accept cookies
* Search for product "asdfghjklqwertyuiop"
* Verify no results message is displayed or result count is zero
