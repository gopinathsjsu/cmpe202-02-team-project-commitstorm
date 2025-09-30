import { useState } from "react";
import Post from "./post.tsx";
import type {ListingDetailProps} from "./post.tsx";
import '../css/Marketplace.css';

const test : ListingDetailProps = {
    id: 1,
    userId: 101,
    username: "john_doe",
    description: "A slightly used textbook on React development.",
    imageUrl: "https://via.placeholder.com/150",
    createdAt: "2023-10-01T12:00:00Z",
    updatedAt: "2023-10-05T15:30:00Z",
    price: 30,
    condition: "Good",
    category: "Textbooks",
};

export const Marketplace = () => {
  return (
    <div className="marketplace-container">
      <Post {...test} />
    </div>
  )
}