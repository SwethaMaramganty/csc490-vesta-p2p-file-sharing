import React, { Component } from 'react';

import {
  Card,
  CardBody,
  Button
} from "shards-react";

import "bootstrap/dist/css/bootstrap.min.css";
import "shards-ui/dist/css/shards.min.css";
import QrCode from 'react.qrcode.generator';


class RecNav extends Component {

  constructor(props) {
    super(props)
    this.state = {
      key: localStorage.getItem("EncSymKeyWithPubKey"),
      fileTransferFlowState: "recieve",
      fromDesktop: true
    }
  }

  render() {
    console.log("ReceivePage EncSymKeyWithPubKey : " + localStorage.getItem("EncSymKeyWithPubKey"))

    const fullLink = window.location.href
    const recLink = fullLink.slice(fullLink.indexOf("receive/") + 8);

    return(
      <div>
        <p style={{color: "#905EAF", position: "absolute", left: "90px", top: "10px", fontSize: "72px"}}> Vesta - Receive </p>
        <p style={{color: "#black", position: "absolute", left: "100px", top: "105px", fontSize: "18px"}}> Secure file sharing in your control  </p>

        <Card style={{position: "absolute", left:"80px", top:"150px", width: "500px"}}>

          <CardBody style={{display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center"}}>
          <h6> <b>Scan the QR below to recieve your files</b> </h6>
          {/* <Button id="sendFileButton" theme="light" style={{color: 'white', borderColor: "#905EAF", backgroundColor: "#905EAF", marginBottom: "20px"}}> Download </Button> */}
          <QrCode value={JSON.stringify(this.state)} size='350'/>
          <a href="/">
          <br/> <br/> <br/> <br/>
          <Button theme="light" style={{color: 'white', borderColor: "#8B0000", backgroundColor: "#8B0000", float:"right"}}> Quit </Button>
          </a>
          </CardBody>
        </Card>
      </div>
    )

  }
}

export default RecNav;
