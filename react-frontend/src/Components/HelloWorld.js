import React, { Component } from 'react';

class HelloWorld extends Component {
    constructor() {
        super();
        this.state = {
            result: null,
            error: null
        };
    }

    componentDidMount() {
        fetch('/api/data') // Replace with your backend API endpoint
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                this.setState({ result: data });
            })
            .catch(error => {
                this.setState({ error: error.message });
            });
    }

    render() {
        const { result, error } = this.state;

        return (
            <div>
                <h1>Hello World</h1>
                {error ? (
                    <div>Error: {error}</div>
                ) : (
                    <div>Result: {result ? JSON.stringify(result) : 'Loading...'}</div>
                )}
            </div>
        );
    }
}

export default HelloWorld;
