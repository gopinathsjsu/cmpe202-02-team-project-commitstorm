import { useState } from 'react'
import {BrowserRouter, Route, Routes} from 'react-router';
import './css/App.css'
import Header from './components/header'
import Home from './components/home'
import { Marketplace } from './components/marketplace';

function App() {

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      <Header />
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Home />}/>
          <Route path="/marketplace" element={<Marketplace/>}/>
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App