import React, { Component } from 'react';

class ExampleComponent extends Component {
    componentDidMount() {
        fetch('/api/data') // Replace with your backend API endpoint
            .then(response => response.json())
            .then(data => console.log(data))
            .catch(error => console.error('Error:', error));
    }

    render() {
        return <div>Hello World! <br /> Check console to see backend JSON file... <br /> USERNAME: user, PASSWORD: password</div>;
    }
}

export default ExampleComponent;