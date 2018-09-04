#ifndef _FILTER_EXCEPTION_H_
#define _FILTER_EXCEPTION_H_

#include <exception>
#include <string>

class FilterException : public std::exception {
public:
	FilterException(const std::string & m):message(m) {

	}

	virtual ~FilterException() throw () {}

	virtual const char * what() const throw () {
		return message.c_str();
	}

private:
	std::string message;
};

#endif
